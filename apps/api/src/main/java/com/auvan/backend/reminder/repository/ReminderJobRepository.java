package com.auvan.backend.reminder.repository;

import com.auvan.backend.reminder.entity.ReminderJob;
import com.auvan.backend.reminder.enums.ReminderStatus;
import com.auvan.backend.reminder.enums.ReminderType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
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
public interface ReminderJobRepository extends JpaRepository<ReminderJob, UUID> {

    /**
     * Fetches pending jobs that are due, with a pessimistic write lock to prevent
     * concurrent workers from picking up the same job.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM ReminderJob r
            WHERE r.status = com.auvan.backend.reminder.enums.ReminderStatus.PENDING
              AND r.runAt <= :now
            ORDER BY r.runAt ASC
            """)
    List<ReminderJob> findPendingJobsDue(@Param("now") Instant now, Pageable pageable);

    boolean existsByBookingIdAndType(UUID bookingId, ReminderType type);

    @Modifying
    @Query("""
            UPDATE ReminderJob r SET r.status = com.auvan.backend.reminder.enums.ReminderStatus.CANCELLED
            WHERE r.booking.id = :bookingId
              AND r.status = com.auvan.backend.reminder.enums.ReminderStatus.PENDING
            """)
    int cancelPendingForBooking(@Param("bookingId") UUID bookingId);

    List<ReminderJob> findByBookingIdAndStatus(UUID bookingId, ReminderStatus status);
}
