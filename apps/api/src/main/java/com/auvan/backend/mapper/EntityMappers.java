package com.auvan.backend.mapper;

import com.auvan.backend.dto.response.AuditLogResponse;
import com.auvan.backend.dto.response.BookingResponse;
import com.auvan.backend.dto.response.NotificationResponse;
import com.auvan.backend.dto.response.PaymentResponse;
import com.auvan.backend.dto.response.RouteResponse;
import com.auvan.backend.dto.response.SeatResponse;
import com.auvan.backend.dto.response.TimeslotResponse;
import com.auvan.backend.dto.response.UserResponse;
import com.auvan.backend.entity.AuditLog;
import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.Notification;
import com.auvan.backend.entity.Payment;
import com.auvan.backend.entity.Route;
import com.auvan.backend.entity.Seat;
import com.auvan.backend.entity.Timeslot;
import com.auvan.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityMappers {

    public RouteResponse toRoute(Route r) {
        return new RouteResponse(
                r.getId(),
                r.getFromLocation(),
                r.getToLocation(),
                r.getSlug(),
                r.getPrice(),
                r.getDurationMinutes(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    public SeatResponse toSeat(Seat s) {
        return new SeatResponse(
                s.getId(),
                s.getSeatNumber(),
                s.getLabel(),
                s.getStatus()
        );
    }

    public TimeslotResponse toTimeslot(Timeslot t) {
        return new TimeslotResponse(
                t.getId(),
                t.getRoute().getId(),
                t.getRoute().getFromLocation(),
                t.getRoute().getToLocation(),
                t.getDate(),
                t.getTime(),
                t.getTotalSeats(),
                t.getBookedSeats(),
                t.getAvailableSeats(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    public PaymentResponse toPayment(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getBooking().getId(),
                p.getAmount(),
                p.getMethod(),
                p.getStatus(),
                p.getTransactionId(),
                p.getProofImageUrl(),
                p.getProofReference(),
                p.getProofSubmittedAt(),
                p.getReviewedBy(),
                p.getReviewedAt(),
                p.getReviewNote(),
                p.getPaidAt(),
                p.getRefundedAt(),
                p.getCreatedAt()
        );
    }

    public BookingResponse toBooking(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getBookingCode(),
                b.getUser().getId(),
                toRoute(b.getRoute()),
                toTimeslot(b.getTimeslot()),
                b.getSeats().stream().map(this::toSeat).toList(),
                b.getPassengers(),
                b.getPassengerName(),
                b.getPassengerPhone(),
                b.getPickupLocation(),
                b.getStatus(),
                b.getPayment() != null ? toPayment(b.getPayment()) : null,
                b.getPaymentDueAt(),
                b.getSourceChannel(),
                b.getRescheduledFromBooking() != null ? b.getRescheduledFromBooking().getId() : null,
                b.getTotalPrice(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    public UserResponse toUser(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getLineUserId(),
                u.getAuthProvider(),
                u.getDisplayName(),
                u.getName(),
                u.getPhone(),
                u.getDefaultPickupLocation(),
                u.getProfileImageUrl(),
                u.isAdmin(),
                u.getCreatedAt()
        );
    }

    public NotificationResponse toNotification(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getChannel(),
                n.getDeliveryStatus(),
                n.getData(),
                n.getCreatedAt()
        );
    }

    public AuditLogResponse toAuditLog(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getActorId(),
                a.getAction(),
                a.getTargetType(),
                a.getTargetId(),
                a.getMetadata(),
                a.getIp(),
                a.getCreatedAt()
        );
    }

    public List<BookingResponse> toBookingList(List<Booking> list) {
        return list.stream().map(this::toBooking).toList();
    }

    public List<RouteResponse> toRouteList(List<Route> list) {
        return list.stream().map(this::toRoute).toList();
    }

    public List<TimeslotResponse> toTimeslotList(List<Timeslot> list) {
        return list.stream().map(this::toTimeslot).toList();
    }

    public List<SeatResponse> toSeatList(List<Seat> list) {
        return list.stream().map(this::toSeat).toList();
    }

    public List<PaymentResponse> toPaymentList(List<Payment> list) {
        return list.stream().map(this::toPayment).toList();
    }

    public List<UserResponse> toUserList(List<User> list) {
        return list.stream().map(this::toUser).toList();
    }

    public List<NotificationResponse> toNotificationList(List<Notification> list) {
        return list.stream().map(this::toNotification).toList();
    }

    public List<AuditLogResponse> toAuditLogList(List<AuditLog> list) {
        return list.stream().map(this::toAuditLog).toList();
    }
}
