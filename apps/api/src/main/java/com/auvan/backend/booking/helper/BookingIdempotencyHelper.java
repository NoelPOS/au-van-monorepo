package com.auvan.backend.booking.helper;

import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.shared.idempotency.IdempotencyKey;
import com.auvan.backend.shared.idempotency.IdempotencyStatus;
import com.auvan.backend.shared.exception.ConflictException;
import com.auvan.backend.shared.exception.ResourceNotFoundException;
import com.auvan.backend.shared.mapper.EntityMappers;
import com.auvan.backend.booking.BookingRepository;
import com.auvan.backend.shared.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class BookingIdempotencyHelper {

    private final IdempotencyService idempotencyService;
    private final BookingRepository bookingRepository;
    private final EntityMappers mappers;

    public BookingResponse run(
            UUID userId,
            String scope,
            String key,
            Object requestBody,
            int successStatusCode,
            Supplier<BookingResponse> action
    ) {
        if (!StringUtils.hasText(key)) {
            return action.get();
        }

        IdempotencyKey existing = idempotencyService.find(userId, scope, key).orElse(null);
        if (existing != null) {
            Optional<BookingResponse> replay = replay(existing);
            if (replay.isPresent()) {
                return replay.get();
            }
        }

        IdempotencyKey request = idempotencyService.startRequest(userId, scope, key, requestBody);
        try {
            BookingResponse response = action.get();
            idempotencyService.completeRequest(request.getId(), response.id().toString(), successStatusCode);
            return response;
        } catch (RuntimeException ex) {
            idempotencyService.failRequest(request.getId(), ex.getMessage());
            throw ex;
        }
    }

    private Optional<BookingResponse> replay(IdempotencyKey key) {
        if (key.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new ConflictException("This request is already being processed", "DUPLICATE_REQUEST");
        }

        if (key.getStatus() != IdempotencyStatus.COMPLETED || key.getResponseData() == null) {
            return Optional.empty();
        }

        UUID bookingId = UUID.fromString(key.getResponseData().toString());
        return Optional.of(bookingRepository.findById(bookingId)
                .map(mappers::toBooking)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId)));
    }
}
