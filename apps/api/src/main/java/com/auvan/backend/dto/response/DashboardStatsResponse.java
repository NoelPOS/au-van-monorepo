package com.auvan.backend.dto.response;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long       totalBookings,
        long       confirmedBookings,
        long       pendingPaymentBookings,
        long       cancelledBookings,
        long       totalPassengers,
        BigDecimal totalRevenue,
        long       totalUsers,
        double     averageOccupancyPercent
) {}
