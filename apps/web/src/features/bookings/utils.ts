export function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function bookingStatusClass(status: string) {
  switch (status) {
    case "CONFIRMED":
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    case "COMPLETED":
      return "border-sky-200 bg-sky-50 text-sky-700";
    case "CANCELLED":
      return "border-slate-200 bg-slate-100 text-slate-600";
    case "PENDING_PAYMENT":
    case "PAYMENT_UNDER_REVIEW":
    default:
      return "border-amber-200 bg-amber-50 text-amber-700";
  }
}

export function canEditBooking(status: string) {
  return status !== "CANCELLED" && status !== "COMPLETED";
}

export function canRescheduleBooking(status: string) {
  return status === "CONFIRMED" || status === "PENDING_PAYMENT";
}

export function paymentStatusClass(status: string) {
  switch (status) {
    case "COMPLETED":
      return "text-emerald-700";
    case "FAILED":
      return "text-rose-700";
    default:
      return "text-amber-700";
  }
}

export function isCancelledBooking(status: string) {
  return status === "CANCELLED";
}

export function isActiveMutableBooking(status: string) {
  return status !== "CANCELLED" && status !== "COMPLETED";
}
