package com.auvan.backend.event;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.enums.CancellationReason;
import org.springframework.context.ApplicationEvent;

public class BookingCancelledEvent extends ApplicationEvent {

    private final Booking            booking;
    private final CancellationReason reason;

    public BookingCancelledEvent(Object source, Booking booking, CancellationReason reason) {
        super(source);
        this.booking = booking;
        this.reason  = reason;
    }

    public Booking            getBooking() { return booking; }
    public CancellationReason getReason()  { return reason; }
}
