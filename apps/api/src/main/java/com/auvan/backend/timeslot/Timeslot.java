package com.auvan.backend.timeslot;

import com.auvan.backend.route.Route;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "timeslots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "date", "time"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int bookedSeats = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeslotStatus status = TimeslotStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }
}
