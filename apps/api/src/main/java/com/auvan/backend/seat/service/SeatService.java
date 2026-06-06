package com.auvan.backend.seat.service;

import com.auvan.backend.seat.entity.Seat;
import com.auvan.backend.seat.dto.SeatResponse;
import java.util.List;
import java.util.UUID;

public interface SeatService {

    /** Returns the full seat map for a timeslot, auto-generating seats if none exist yet. */
    List<SeatResponse> getSeatMap(UUID timeslotId);

    /**
     * Acquires pessimistic locks on the requested seats.
     * Throws {@link SeatLockException} if any seat is unavailable.
     */
    List<Seat> lockSeats(UUID timeslotId, List<UUID> seatIds, UUID userId);

    /** Transitions locked seats to booked and increments timeslot.bookedSeats. */
    void confirmSeats(List<UUID> seatIds, UUID userId);

    /** Releases locked or booked seats back to available. */
    void freeSeats(List<UUID> seatIds);

    /** Bulk-releases all expired seat locks (called by the cleanup scheduler). */
    int releaseExpiredLocks();
}
