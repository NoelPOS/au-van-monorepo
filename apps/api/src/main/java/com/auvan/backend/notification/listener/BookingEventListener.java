package com.auvan.backend.notification.listener;

import com.auvan.backend.booking.Booking;
import com.auvan.backend.user.User;
import com.auvan.backend.booking.CancellationReason;
import com.auvan.backend.notification.NotificationType;
import com.auvan.backend.booking.event.BookingCancelledEvent;
import com.auvan.backend.booking.event.BookingConfirmedEvent;
import com.auvan.backend.booking.event.BookingCreatedEvent;
import com.auvan.backend.user.UserRepository;
import com.auvan.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        Booking booking = event.getBooking();
        User user = booking.getUser();

        String route = routeSummary(booking);
        notificationService.sendInApp(
                user,
                NotificationType.BOOKING_UPDATED,
                "Booking Created",
                "Your booking " + booking.getBookingCode() + " for " + route + " has been created.",
                basePayload(booking)
        );

        userRepository.findByIsAdminTrue().forEach(admin ->
                notificationService.sendInApp(
                        admin,
                        NotificationType.ADMIN_NEW_BOOKING,
                        "New Booking",
                        "Booking " + booking.getBookingCode() + " was created by " + user.getName() + ".",
                        basePayload(booking)
                )
        );
    }

    @Async
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        notificationService.sendInApp(
                booking.getUser(),
                NotificationType.BOOKING_CONFIRMED,
                "Booking Confirmed",
                "Payment approved. Booking " + booking.getBookingCode() + " is confirmed.",
                basePayload(booking)
        );
    }

    @Async
    @EventListener
    public void onBookingCancelled(BookingCancelledEvent event) {
        Booking booking = event.getBooking();
        CancellationReason reason = event.getReason();

        notificationService.sendInApp(
                booking.getUser(),
                NotificationType.BOOKING_CANCELLED,
                "Booking Cancelled",
                "Booking " + booking.getBookingCode() + " has been cancelled (" + reason.name() + ").",
                basePayload(booking)
        );

        if (reason == CancellationReason.ADMIN_CANCELLED) {
            UUID actorId = booking.getUser().getId();
            userRepository.findByIsAdminTrue().forEach(admin ->
                    notificationService.sendInApp(
                            admin,
                            NotificationType.ADMIN_CANCELLATION,
                            "Booking Cancelled By Admin",
                            "Booking " + booking.getBookingCode() + " has been cancelled by an admin.",
                            Map.of("bookingId", booking.getId(), "userId", actorId)
                    )
            );
        }

        log.debug("Processed booking cancellation event for {}", booking.getId());
    }

    private static Map<String, Object> basePayload(Booking booking) {
        return Map.of(
                "bookingId", booking.getId(),
                "bookingCode", booking.getBookingCode(),
                "routeId", booking.getRoute().getId(),
                "timeslotId", booking.getTimeslot().getId()
        );
    }

    private static String routeSummary(Booking booking) {
        return booking.getRoute().getFromLocation() + " to " + booking.getRoute().getToLocation();
    }
}
