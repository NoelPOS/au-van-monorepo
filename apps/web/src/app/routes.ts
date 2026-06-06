export const appRoutes = {
  home: "/",
  routes: "/routes",
  myBookings: "/my-bookings",
  notifications: "/notifications",
  profile: "/profile",
  auth: "/auth",
  admin: "/admin",
  adminBookings: "/admin/bookings",
  adminPayments: "/admin/payments",
  adminRoutes: "/admin/routes",
  adminTimeslots: "/admin/timeslots",
  adminUsers: "/admin/users",
  adminAuditLogs: "/admin/audit-logs",
  book: (routeId: string) => `/book/${routeId}`,
  bookingDetails: (bookingId: string) => `/my-bookings/${bookingId}`,
  payment: (bookingId: string) => `/payment/${bookingId}`,
} as const;

export const liffNavItems = [
  { to: appRoutes.home, label: "Home" },
  { to: appRoutes.routes, label: "Routes" },
  { to: appRoutes.myBookings, label: "My Bookings" },
  { to: appRoutes.notifications, label: "Notifications" },
  { to: appRoutes.profile, label: "Profile" },
  { to: appRoutes.auth, label: "Admin" },
] as const;

export const adminNavItems = [
  { to: appRoutes.admin, label: "Dashboard" },
  { to: appRoutes.adminBookings, label: "Bookings" },
  { to: appRoutes.adminPayments, label: "Payments" },
  { to: appRoutes.adminRoutes, label: "Routes" },
  { to: appRoutes.adminTimeslots, label: "Timeslots" },
  { to: appRoutes.adminUsers, label: "Users" },
  { to: appRoutes.adminAuditLogs, label: "Audit Logs" },
] as const;
