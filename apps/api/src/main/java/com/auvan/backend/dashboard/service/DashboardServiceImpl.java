package com.auvan.backend.dashboard.service;

import com.auvan.backend.dashboard.dto.DashboardStatsResponse;
import com.auvan.backend.booking.BookingStatus;
import com.auvan.backend.payment.PaymentStatus;
import com.auvan.backend.booking.BookingRepository;
import com.auvan.backend.payment.PaymentRepository;
import com.auvan.backend.timeslot.TimeslotRepository;
import com.auvan.backend.user.UserRepository;
import com.auvan.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository  bookingRepository;
    private final PaymentRepository  paymentRepository;
    private final UserRepository     userRepository;
    private final TimeslotRepository timeslotRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long total      = bookingRepository.count();
        long confirmed  = countByStatus(BookingStatus.CONFIRMED);
        long pending    = countByStatus(BookingStatus.PENDING_PAYMENT);
        long cancelled  = countByStatus(BookingStatus.CANCELLED);
        long users      = userRepository.count();

        // Sum passengers from confirmed bookings
        long passengers = bookingRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                          || b.getStatus() == BookingStatus.COMPLETED)
                .mapToLong(b -> b.getPassengers())
                .sum();

        // Revenue from completed payments
        BigDecimal revenue = paymentRepository
                .findByStatusOrderByCreatedAtDesc(PaymentStatus.COMPLETED, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average occupancy from all timeslots
        double avgOccupancy = timeslotRepository.findAll().stream()
                .filter(t -> t.getTotalSeats() > 0)
                .mapToDouble(t -> (double) t.getBookedSeats() / t.getTotalSeats() * 100)
                .average()
                .orElse(0.0);

        return new DashboardStatsResponse(
                total, confirmed, pending, cancelled,
                passengers, revenue, users,
                Math.round(avgOccupancy * 100.0) / 100.0
        );
    }

    private long countByStatus(BookingStatus status) {
        return bookingRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }
}
