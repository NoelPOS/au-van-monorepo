package com.auvan.backend.booking.service;

import com.auvan.backend.booking.dto.CreateBookingRequest;
import com.auvan.backend.booking.dto.RescheduleRequest;
import com.auvan.backend.booking.dto.UpdateBookingRequest;
import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.shared.dto.PageResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(UUID userId, CreateBookingRequest request);

    BookingResponse getById(UUID bookingId, UUID requestingUserId);

    List<BookingResponse> getMyBookings(UUID userId);

    PageResponse<BookingResponse> listAll(int page, int size);

    BookingResponse update(UUID bookingId, UUID userId, UpdateBookingRequest request);

    void cancel(UUID bookingId, UUID requestingUserId, boolean isAdmin);

    BookingResponse reschedule(UUID bookingId, UUID userId, RescheduleRequest request);

    /** Called by the internal worker endpoint and scheduler to expire unpaid bookings. */
    int expireUnpaidBookings(int limit);
}
