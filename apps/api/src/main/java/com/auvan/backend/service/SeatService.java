package com.auvan.backend.service;

import com.auvan.backend.dto.response.SeatResponse;
import com.auvan.backend.entity.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatService {

    /** Returns the full seat map for a timeslot, auto-generating seats if none exist yet. */
    List<SeatResponse> getSeatMap(UUID timeslotId);

    /**
     * Acquires pessimistic locks on the requested seats.
     * Throws {@link com.auvan.backend.exception.SeatLockException} if any seat is unavailable.
     */
    List<Seat> lockSeats(UUID timeslotId, List<UUID> seatIds, UUID userId);

    /** Transitions locked seats → booked and increments timeslot.bookedSeats. */
    void confirmSeats(List<UUID> seatIds, UUID userId);

    /** Releases locked or booked seats back to available. */
    void freeSeats(List<UUID> seatIds);

    /** Bulk-releases all expired seat locks (called by the cleanup scheduler). */
    int releaseExpiredLocks();
}
