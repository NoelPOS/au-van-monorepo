package com.auvan.backend.booking.entity;

import com.auvan.backend.booking.enums.BookingStatus;
import com.auvan.backend.booking.enums.SourceChannel;
import com.auvan.backend.payment.entity.Payment;
import com.auvan.backend.route.entity.Route;
import com.auvan.backend.seat.entity.Seat;
import com.auvan.backend.timeslot.entity.Timeslot;
import com.auvan.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timeslot_id", nullable = false)
    private Timeslot timeslot;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats = new ArrayList<>();

    @Column(nullable = false)
    private int passengers;

    @Column(nullable = false)
    private String passengerName;

    @Column(nullable = false, length = 50)
    private String passengerPhone;

    @Column(nullable = false, length = 500)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status = BookingStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Instant paymentDueAt;

    @Column(unique = true, length = 20)
    private String bookingCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SourceChannel sourceChannel = SourceChannel.LIFF;

    /** Self-reference: the original booking this was rescheduled from */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from_booking_id")
    private Booking rescheduledFromBooking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
