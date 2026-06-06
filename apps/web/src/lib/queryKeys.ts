export const bookingKeys = {
  me: () => ["bookings", "me"] as const,
  detail: (id: string) => ["bookings", id] as const,
  admin: (page = 0, size = 20) => ["admin", "bookings", page, size] as const,
  adminAll: () => ["admin", "bookings"] as const,
};

export const routeKeys = {
  all: () => ["routes"] as const,
  detail: (id: string) => ["route", id] as const,
  admin: () => ["admin", "routes"] as const,
};
