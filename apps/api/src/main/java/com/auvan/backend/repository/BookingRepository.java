package com.auvan.backend.repository;

import com.auvan.backend.entity.Booking;
import com.auvan.backend.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Booking> findByUserIdAndStatusNotInOrderByCreatedAtDesc(
            UUID userId, List<BookingStatus> excludedStatuses);

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);

    /** Finds unpaid bookings whose payment deadline has passed. */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status IN ('PENDING_PAYMENT', 'PAYMENT_UNDER_REVIEW')
              AND b.paymentDueAt IS NOT NULL
              AND b.paymentDueAt < :now
            ORDER BY b.paymentDueAt ASC
            """)
    List<Booking> findExpiredUnpaidBookings(@Param("now") Instant now, Pageable pageable);

    /** Active bookings on a specific timeslot (for overlap detection). */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.id = :userId
              AND b.timeslot.id = :timeslotId
              AND b.status NOT IN ('CANCELLED', 'COMPLETED')
              AND (:excludeId IS NULL OR b.id <> :excludeId)
            """)
    List<Booking> findActiveBookingsForUserOnTimeslot(
            @Param("userId") UUID userId,
            @Param("timeslotId") UUID timeslotId,
            @Param("excludeId") UUID excludeId);
}
