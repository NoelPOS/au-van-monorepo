package com.auvan.backend.seat;

import com.auvan.backend.timeslot.Timeslot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"timeslot_id", "seat_number"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timeslot_id", nullable = false)
    private Timeslot timeslot;

    @Column(nullable = false)
    private int seatNumber;

    /** Auto-generated label: 1A, 1B, 2A, etc. (row * 4 columns) */
    @Column(nullable = false, length = 10)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    /**
     * Stored as UUID column rather than @ManyToOne to avoid N+1 fetches.
     * The full User can be loaded on demand if needed.
     */
    @Column(name = "locked_by")
    private UUID lockedBy;

    private Instant lockedAt;

    @Column(name = "booked_by")
    private UUID bookedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Generates the seat label from the 1-based seat number.
     * Layout: 4 seats per row (A, B, C, D).
     * Example: seatNumber=1 → "1A", seatNumber=5 → "2A"
     */
    public static String buildLabel(int seatNumber) {
        int row = (seatNumber - 1) / 4 + 1;
        char col = (char) ('A' + (seatNumber - 1) % 4);
        return row + String.valueOf(col);
    }
}
