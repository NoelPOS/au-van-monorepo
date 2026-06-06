package com.auvan.backend.seat.service;

import com.auvan.backend.shared.mapper.EntityMappers;

import com.auvan.backend.seat.dto.SeatResponse;
import com.auvan.backend.seat.Seat;
import com.auvan.backend.seat.SeatStatus;
import com.auvan.backend.shared.exception.ResourceNotFoundException;
import com.auvan.backend.shared.exception.SeatLockException;

import com.auvan.backend.seat.SeatRepository;
import com.auvan.backend.timeslot.TimeslotRepository;
import com.auvan.backend.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    @Value("${seat.lock.timeout.seconds:300}")
    private int seatLockTimeoutSeconds;

    private final SeatRepository     seatRepository;
    private final TimeslotRepository timeslotRepository;
    private final EntityMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatMap(UUID timeslotId) {
        List<Seat> seats = seatRepository.findByTimeslotIdOrderBySeatNumberAsc(timeslotId);
        if (seats.isEmpty()) {
            throw ResourceNotFoundException.of("Timeslot seats", timeslotId);
        }
        return mappers.toSeatList(seats);
    }

    @Override
    @Transactional
    public List<Seat> lockSeats(UUID timeslotId, List<UUID> seatIds, UUID userId) {
        // Release any stale locks first to maximise availability
        releaseExpiredLocks();

        List<Seat> seats = seatRepository.findByIdInAndTimeslotIdWithLock(seatIds, timeslotId);

        if (seats.size() != seatIds.size()) {
            throw new SeatLockException("One or more requested seats do not belong to this timeslot");
        }

        List<UUID> unavailableSeatIds = seats.stream()
                .filter(s -> s.getStatus() != SeatStatus.AVAILABLE)
                .map(Seat::getId)
                .toList();

        if (!unavailableSeatIds.isEmpty()) {
            throw new SeatLockException("Seats are no longer available: " + unavailableSeatIds);
        }

        Instant now = Instant.now();
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedBy(userId);
            seat.setLockedAt(now);
        }

        return seatRepository.saveAll(seats);
    }

    @Override
    @Transactional
    public void confirmSeats(List<UUID> seatIds, UUID userId) {
        if (seatIds.isEmpty()) {
            return;
        }

        List<Seat> seats = seatRepository.findByIdInAndLockedByAndStatus(
                seatIds, userId, SeatStatus.LOCKED);

        UUID timeslotId = seats.isEmpty() ? null : seats.get(0).getTimeslot().getId();
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookedBy(userId);
            seat.setLockedBy(null);
            seat.setLockedAt(null);
        }

        seatRepository.saveAll(seats);

        if (timeslotId != null) {
            timeslotRepository.adjustBookedSeats(timeslotId, seats.size());
            timeslotRepository.markFullIfSoldOut(timeslotId);
        }
    }

    @Override
    @Transactional
    public void freeSeats(List<UUID> seatIds) {
        if (seatIds.isEmpty()) {
            return;
        }

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.isEmpty()) {
            return;
        }

        UUID timeslotId = seats.get(0).getTimeslot().getId();
        long bookedCount = seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.BOOKED)
                .count();

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockedAt(null);
            seat.setBookedBy(null);
        }

        seatRepository.saveAll(seats);

        if (bookedCount > 0) {
            timeslotRepository.adjustBookedSeats(timeslotId, -(int) bookedCount);
            timeslotRepository.reactivateIfNotFull(timeslotId);
        }
    }

    @Override
    @Transactional
    public int releaseExpiredLocks() {
        Instant cutoff = Instant.now().minusSeconds(seatLockTimeoutSeconds);
        int count = seatRepository.releaseExpiredLocks(cutoff);
        if (count > 0) {
            log.debug("Released {} expired seat locks", count);
        }
        return count;
    }
}
