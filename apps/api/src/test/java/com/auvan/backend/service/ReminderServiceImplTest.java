package com.auvan.backend.service;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.ReminderJob;
import com.auvan.backend.entity.Route;
import com.auvan.backend.entity.Timeslot;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.NotificationType;
import com.auvan.backend.enums.ReminderStatus;
import com.auvan.backend.enums.ReminderType;
import com.auvan.backend.repository.BookingRepository;
import com.auvan.backend.repository.ReminderJobRepository;
import com.auvan.backend.service.impl.ReminderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock private ReminderJobRepository reminderJobRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ReminderServiceImpl reminderService;

    private Booking booking;

    @BeforeEach
    void setup() {
        booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setBookingCode("AUV-260329-ABCDE");

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        booking.setRoute(route);

        Timeslot timeslot = new Timeslot();
        timeslot.setDate(LocalDate.now().plusDays(2));
        timeslot.setTime(LocalTime.of(9, 0));
        booking.setTimeslot(timeslot);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Alice");
        user.setLineUserId("line-123");
        booking.setUser(user);
    }

    @Test
    void scheduleForBookingTimedModeCreatesTwoJobs() {
        setField(reminderService, "scheduleMode", "timed");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(reminderJobRepository.save(any(ReminderJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(reminderJobRepository.existsByBookingIdAndType(booking.getId(), ReminderType.DEPARTURE_24H)).thenReturn(false);
        when(reminderJobRepository.existsByBookingIdAndType(booking.getId(), ReminderType.DEPARTURE_1H)).thenReturn(false);

        reminderService.scheduleForBooking(booking.getId());

        ArgumentCaptor<ReminderJob> captor = ArgumentCaptor.forClass(ReminderJob.class);
        verify(reminderJobRepository, times(2)).save(captor.capture());
        List<ReminderType> createdTypes = captor.getAllValues().stream().map(ReminderJob::getType).toList();
        assertThat(createdTypes).containsExactlyInAnyOrder(
                ReminderType.DEPARTURE_24H, ReminderType.DEPARTURE_1H
        );
    }

    @Test
    void scheduleForBookingDailyBatchCreatesSingleJob() {
        setField(reminderService, "scheduleMode", "daily_batch");
        setField(reminderService, "batchHourUtc", 1);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(reminderJobRepository.save(any(ReminderJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(reminderJobRepository.existsByBookingIdAndType(booking.getId(), ReminderType.DEPARTURE_DAILY_BATCH))
                .thenReturn(false);

        reminderService.scheduleForBooking(booking.getId());

        ArgumentCaptor<ReminderJob> captor = ArgumentCaptor.forClass(ReminderJob.class);
        verify(reminderJobRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ReminderType.DEPARTURE_DAILY_BATCH);
    }

    @Test
    void processPendingRemindersMarksJobSent() {
        setField(reminderService, "maxAttempts", 5);

        ReminderJob job = new ReminderJob();
        job.setId(UUID.randomUUID());
        job.setBooking(booking);
        job.setUser(booking.getUser());
        job.setType(ReminderType.DEPARTURE_1H);
        job.setStatus(ReminderStatus.PENDING);
        job.setRunAt(Instant.now().minusSeconds(60));
        job.setAttempts(0);

        when(reminderJobRepository.findPendingJobsDue(any(), any())).thenReturn(List.of(job));
        when(reminderJobRepository.save(any(ReminderJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int processed = reminderService.processPendingReminders(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(ReminderStatus.SENT);
        verify(notificationService).sendInApp(
                eq(booking.getUser()),
                eq(NotificationType.SEAT_REMINDER),
                any(),
                any(),
                any(Map.class)
        );
    }
}
