import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { AdminLayout } from "@/components/layout/AdminLayout";
import { AppShell } from "@/components/layout/AppShell";
import { LiffLayout } from "@/components/layout/LiffLayout";
import { AdminAuditLogsPage } from "@/pages/admin/AdminAuditLogsPage";
import { AdminBookingsPage } from "@/pages/admin/AdminBookingsPage";
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
import { AdminPaymentsPage } from "@/pages/admin/AdminPaymentsPage";
import { AdminRoutesPage } from "@/pages/admin/AdminRoutesPage";
import { AdminTimeslotsPage } from "@/pages/admin/AdminTimeslotsPage";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage";
import { AuthPage } from "@/pages/auth/AuthPage";
import { BookRoutePage } from "@/pages/liff/BookRoutePage";
import { BookingDetailsPage } from "@/pages/liff/BookingDetailsPage";
import { LiffHomePage } from "@/pages/liff/LiffHomePage";
import { MyBookingsPage } from "@/pages/liff/MyBookingsPage";
import { NotificationsPage } from "@/pages/liff/NotificationsPage";
import { PaymentProofPage } from "@/pages/liff/PaymentProofPage";
import { ProfilePage } from "@/pages/liff/ProfilePage";
import { NotFoundPage } from "@/pages/NotFoundPage";

const router = createBrowserRouter([
  {
    path: "/",
    element: <AppShell />,
    errorElement: <NotFoundPage />,
    children: [
      {
        element: <LiffLayout />,
        children: [
          { index: true, element: <LiffHomePage /> },
          { path: appRoutes.routes.slice(1), element: <LiffHomePage /> },
          { path: appRoutes.myBookings.slice(1), element: <MyBookingsPage /> },
          { path: "my-bookings/:bookingId", element: <BookingDetailsPage /> },
          { path: appRoutes.notifications.slice(1), element: <NotificationsPage /> },
          { path: appRoutes.profile.slice(1), element: <ProfilePage /> },
          { path: "payment/:bookingId", element: <PaymentProofPage /> },
          { path: "book/:routeId", element: <BookRoutePage /> },
        ],
      },
      {
        path: appRoutes.auth.slice(1),
        element: <AuthPage />,
      },
      {
        path: appRoutes.admin.slice(1),
        element: <AdminLayout />,
        children: [
          { index: true, element: <AdminDashboardPage /> },
          { path: "bookings", element: <AdminBookingsPage /> },
          { path: "payments", element: <AdminPaymentsPage /> },
          { path: "routes", element: <AdminRoutesPage /> },
          { path: "timeslots", element: <AdminTimeslotsPage /> },
          { path: "users", element: <AdminUsersPage /> },
          { path: "audit-logs", element: <AdminAuditLogsPage /> },
        ],
      },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
