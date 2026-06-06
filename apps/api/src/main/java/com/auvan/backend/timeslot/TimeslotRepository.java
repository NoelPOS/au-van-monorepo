package com.auvan.backend.timeslot;

import com.auvan.backend.timeslot.Timeslot;
import com.auvan.backend.timeslot.TimeslotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, UUID> {

    List<Timeslot> findByRouteIdAndDateGreaterThanEqualAndStatusOrderByDateAscTimeAsc(
            UUID routeId, LocalDate from, TimeslotStatus status);

    List<Timeslot> findByRouteIdAndDateAndStatusOrderByTimeAsc(
            UUID routeId, LocalDate date, TimeslotStatus status);

    @Modifying
    @Query("UPDATE Timeslot t SET t.bookedSeats = t.bookedSeats + :delta WHERE t.id = :id")
    void adjustBookedSeats(@Param("id") UUID id, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Timeslot t SET t.status = 'FULL' WHERE t.id = :id AND t.bookedSeats >= t.totalSeats")
    void markFullIfSoldOut(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Timeslot t SET t.status = 'ACTIVE' WHERE t.id = :id AND t.status = 'FULL' AND t.bookedSeats < t.totalSeats")
    void reactivateIfNotFull(@Param("id") UUID id);
}
