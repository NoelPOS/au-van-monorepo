package com.auvan.backend.booking.dto;

import com.auvan.backend.payment.enums.PaymentMethod;
import com.auvan.backend.booking.enums.SourceChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotNull(message = "Timeslot ID is required")
        UUID timeslotId,

        @NotEmpty(message = "At least one seat must be selected")
        @Size(max = 10, message = "Cannot book more than 10 seats at once")
        List<UUID> seatIds,

        @NotBlank(message = "Passenger name is required")
        String passengerName,

        @NotBlank(message = "Passenger phone is required")
        String passengerPhone,

        @NotBlank(message = "Pickup location is required")
        String pickupLocation,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        SourceChannel sourceChannel,

        /** Client-provided idempotency key to prevent duplicate bookings */
        String idempotencyKey
) {}
