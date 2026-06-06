package com.auvan.backend.dto.response;

import com.auvan.backend.enums.BookingStatus;
import com.auvan.backend.enums.SourceChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID            id,
        String          bookingCode,
        UUID            userId,
        RouteResponse   route,
        TimeslotResponse timeslot,
        List<SeatResponse> seats,
        int             passengers,
        String          passengerName,
        String          passengerPhone,
        String          pickupLocation,
        BookingStatus   status,
        PaymentResponse payment,
        Instant         paymentDueAt,
        SourceChannel   sourceChannel,
        UUID            rescheduledFromBookingId,
        BigDecimal      totalPrice,
        Instant         createdAt,
        Instant         updatedAt
) {}
