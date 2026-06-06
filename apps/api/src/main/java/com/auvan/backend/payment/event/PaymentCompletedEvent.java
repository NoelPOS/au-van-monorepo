package com.auvan.backend.payment.event;

import com.auvan.backend.booking.Booking;
import com.auvan.backend.payment.Payment;
import org.springframework.context.ApplicationEvent;

public class PaymentCompletedEvent extends ApplicationEvent {

    private final Payment payment;
    private final Booking booking;

    public PaymentCompletedEvent(Object source, Payment payment, Booking booking) {
        super(source);
        this.payment = payment;
        this.booking = booking;
    }

    public Payment getPayment() { return payment; }
    public Booking getBooking() { return booking; }
}
