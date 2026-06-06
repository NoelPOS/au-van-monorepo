package com.auvan.backend.dto.request;

public record UpdateBookingRequest(
        String passengerName,
        String passengerPhone,
        String pickupLocation
) {}
