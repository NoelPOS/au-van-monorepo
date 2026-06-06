package com.auvan.backend.booking.dto;

public record UpdateBookingRequest(
        String passengerName,
        String passengerPhone,
        String pickupLocation
) {}
