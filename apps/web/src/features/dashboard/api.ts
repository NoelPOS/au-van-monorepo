import { apiRequest } from "@/api/client";

export type DashboardStats = {
  totalBookings: number;
  confirmedBookings: number;
  pendingPaymentBookings: number;
  cancelledBookings: number;
  totalPassengers: number;
  totalRevenue: number;
  totalUsers: number;
  averageOccupancyPercent: number;
};

export function getDashboardStats() {
  return apiRequest<DashboardStats>("/api/admin/dashboard/stats");
}
