package com.auvan.backend.seat.repository;

import com.auvan.backend.seat.entity.Seat;
import com.auvan.backend.seat.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByTimeslotIdOrderBySeatNumberAsc(UUID timeslotId);

    List<Seat> findByTimeslotIdAndStatusOrderBySeatNumberAsc(UUID timeslotId, SeatStatus status);

    /**
     * Acquires pessimistic write locks on the requested seats for a given timeslot.
     * Prevents concurrent booking of the same seats.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.timeslot.id = :timeslotId")
    List<Seat> findByIdInAndTimeslotIdWithLock(
            @Param("ids") List<UUID> ids,
            @Param("timeslotId") UUID timeslotId);

    List<Seat> findByIdInAndLockedByAndStatus(
            List<UUID> ids, UUID lockedBy, SeatStatus status);

    /**
     * Bulk-releases seat locks that have expired past the given cutoff timestamp.
     */
    @Modifying
    @Query("""
            UPDATE Seat s
            SET s.status   = com.auvan.backend.seat.enums.SeatStatus.AVAILABLE,
                s.lockedBy = null,
                s.lockedAt = null
            WHERE s.status  = com.auvan.backend.seat.enums.SeatStatus.LOCKED
              AND s.lockedAt < :cutoff
            """)
    int releaseExpiredLocks(@Param("cutoff") Instant cutoff);
}
