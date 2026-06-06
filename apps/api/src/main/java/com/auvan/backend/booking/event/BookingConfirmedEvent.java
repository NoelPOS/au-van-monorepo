package com.auvan.backend.booking.event;

import com.auvan.backend.booking.entity.Booking;
import org.springframework.context.ApplicationEvent;

public class BookingConfirmedEvent extends ApplicationEvent {

    private final Booking booking;

    public BookingConfirmedEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
