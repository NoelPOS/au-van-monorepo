package com.auvan.backend.event.listener;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.enums.NotificationType;
import com.auvan.backend.event.PaymentCompletedEvent;
import com.auvan.backend.event.PaymentFailedEvent;
import com.auvan.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Booking booking = event.getBooking();
        notificationService.sendInApp(
                booking.getUser(),
                NotificationType.PAYMENT_RECEIVED,
                "Payment Approved",
                "Payment for booking " + booking.getBookingCode() + " has been approved.",
                Map.of("bookingId", booking.getId(), "paymentId", event.getPayment().getId())
        );
    }

    @Async
    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        Booking booking = event.getBooking();
        notificationService.sendInApp(
                booking.getUser(),
                NotificationType.PAYMENT_FAILED,
                "Payment Rejected",
                "Payment for booking " + booking.getBookingCode() + " was not approved.",
                Map.of("bookingId", booking.getId(), "paymentId", event.getPayment().getId())
        );
    }
}
