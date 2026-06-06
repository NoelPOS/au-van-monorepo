package com.auvan.backend.booking;

public enum BookingStatus {
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_UNDER_REVIEW,
    CONFIRMED,
    RESCHEDULE_REQUESTED,
    CANCELLED,
    COMPLETED
}
