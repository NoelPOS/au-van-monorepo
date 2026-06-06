package com.auvan.backend.service.impl;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.ReminderJob;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.NotificationType;
import com.auvan.backend.enums.ReminderStatus;
import com.auvan.backend.enums.ReminderType;
import com.auvan.backend.exception.ResourceNotFoundException;
import com.auvan.backend.repository.BookingRepository;
import com.auvan.backend.repository.ReminderJobRepository;
import com.auvan.backend.service.NotificationService;
import com.auvan.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    @Value("${reminder.schedule.mode:daily_batch}")
    private String scheduleMode;

    @Value("${reminder.batch.hour.utc:1}")
    private int batchHourUtc;

    @Value("${reminder.max.attempts:5}")
    private int maxAttempts;

    private final ReminderJobRepository reminderJobRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void scheduleForBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        Instant departureTime = LocalDateTime
                .of(booking.getTimeslot().getDate(), booking.getTimeslot().getTime())
                .toInstant(ZoneOffset.UTC);

        if ("timed".equalsIgnoreCase(scheduleMode)) {
            scheduleTimedReminders(booking, departureTime);
        } else {
            scheduleDailyBatchReminder(booking);
        }
    }

    @Override
    @Transactional
    public void cancelForBooking(UUID bookingId) {
        int count = reminderJobRepository.cancelPendingForBooking(bookingId);
        if (count > 0) {
            log.debug("Cancelled {} reminder jobs for booking {}", count, bookingId);
        }
    }

    @Override
    @Transactional
    public int processPendingReminders(int limit) {
        List<ReminderJob> jobs = reminderJobRepository.findPendingJobsDue(
                Instant.now(), PageRequest.of(0, limit));

        int processed = 0;
        for (ReminderJob job : jobs) {
            try {
                job.setStatus(ReminderStatus.PROCESSING);
                job.setLockedAt(Instant.now());
                reminderJobRepository.save(job);

                sendReminderNotification(job);

                job.setStatus(ReminderStatus.SENT);
                job.setSentAt(Instant.now());
                job.setLockedAt(null);
                processed++;
            } catch (Exception ex) {
                log.error(
                        "Reminder job {} failed (attempt {}): {}",
                        job.getId(),
                        job.getAttempts() + 1,
                        ex.getMessage()
                );

                job.setAttempts(job.getAttempts() + 1);
                job.setLastError(ex.getMessage());
                job.setLockedAt(null);

                if (job.getAttempts() >= maxAttempts) {
                    job.setStatus(ReminderStatus.FAILED);
                    log.warn("Reminder job {} permanently failed after {} attempts", job.getId(), maxAttempts);
                } else {
                    job.setStatus(ReminderStatus.PENDING);
                    job.setRunAt(Instant.now().plusSeconds(300L * job.getAttempts()));
                }
            }

            reminderJobRepository.save(job);
        }

        return processed;
    }

    private void scheduleTimedReminders(Booking booking, Instant departureTime) {
        createJobIfAbsent(booking, ReminderType.DEPARTURE_24H, departureTime.minusSeconds(86_400));
        createJobIfAbsent(booking, ReminderType.DEPARTURE_1H, departureTime.minusSeconds(3_600));
    }

    private void scheduleDailyBatchReminder(Booking booking) {
        LocalDate dayBefore = booking.getTimeslot().getDate().minusDays(1);
        Instant runAt = LocalDateTime.of(dayBefore, LocalTime.of(batchHourUtc, 0))
                .toInstant(ZoneOffset.UTC);

        if (runAt.isBefore(Instant.now())) {
            runAt = LocalDateTime.of(LocalDate.now(), LocalTime.of(batchHourUtc, 0))
                    .toInstant(ZoneOffset.UTC);
            if (runAt.isBefore(Instant.now())) {
                runAt = runAt.plusSeconds(86_400);
            }
        }

        createJobIfAbsent(booking, ReminderType.DEPARTURE_DAILY_BATCH, runAt);
    }

    private void createJobIfAbsent(Booking booking, ReminderType type, Instant runAt) {
        if (reminderJobRepository.existsByBookingIdAndType(booking.getId(), type)) {
            return;
        }

        if (runAt.isBefore(Instant.now())) {
            log.debug("Skipping {} reminder for booking {} - runAt is in the past", type, booking.getId());
            return;
        }

        ReminderJob job = new ReminderJob();
        job.setBooking(booking);
        job.setUser(booking.getUser());
        job.setLineUserId(booking.getUser().getLineUserId());
        job.setType(type);
        job.setRunAt(runAt);
        job.setStatus(ReminderStatus.PENDING);
        job.setPayload(Map.of(
                "bookingCode", booking.getBookingCode(),
                "routeFrom", booking.getRoute().getFromLocation(),
                "routeTo", booking.getRoute().getToLocation(),
                "departureDate", booking.getTimeslot().getDate().toString(),
                "departureTime", booking.getTimeslot().getTime().toString()
        ));

        reminderJobRepository.save(job);
    }

    private void sendReminderNotification(ReminderJob job) {
        User user = job.getUser();
        String bookingCode = job.getBooking().getBookingCode();
        String route = job.getBooking().getRoute().getFromLocation()
                + " to " + job.getBooking().getRoute().getToLocation();

        String title = "Departure Reminder - " + route;
        String message = switch (job.getType()) {
            case DEPARTURE_24H -> "Your trip from " + route + " departs in 24 hours. Booking: " + bookingCode;
            case DEPARTURE_1H -> "Your trip from " + route + " departs in 1 hour. Booking: " + bookingCode;
            case DEPARTURE_DAILY_BATCH -> "Reminder: You have a trip from " + route + " tomorrow. Booking: " + bookingCode;
        };

        notificationService.sendInApp(
                user,
                NotificationType.SEAT_REMINDER,
                title,
                message,
                Map.of(
                        "bookingId", job.getBooking().getId().toString(),
                        "bookingCode", bookingCode
                )
        );
    }
}
