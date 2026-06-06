package com.auvan.backend.booking.dto;

import com.auvan.backend.booking.BookingStatus;
import com.auvan.backend.booking.SourceChannel;
import com.auvan.backend.payment.dto.PaymentResponse;
import com.auvan.backend.route.dto.RouteResponse;
import com.auvan.backend.seat.dto.SeatResponse;
import com.auvan.backend.timeslot.dto.TimeslotResponse;

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
