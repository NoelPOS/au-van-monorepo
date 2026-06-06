package com.auvan.backend.event;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.Payment;
import org.springframework.context.ApplicationEvent;

public class PaymentFailedEvent extends ApplicationEvent {

    private final Payment payment;
    private final Booking booking;

    public PaymentFailedEvent(Object source, Payment payment, Booking booking) {
        super(source);
        this.payment = payment;
        this.booking = booking;
    }

    public Payment getPayment() { return payment; }
    public Booking getBooking() { return booking; }
}
