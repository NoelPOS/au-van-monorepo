export type RouteSummary = {
  id: string;
  from: string;
  to: string;
  price: number;
  durationMinutes?: number | null;
  slug?: string;
  status?: string;
};

export type TimeslotSummary = {
  id: string;
  date: string;
  time: string;
  totalSeats: number;
  bookedSeats: number;
  availableSeats: number;
  status?: string;
};

export type SeatSummary = {
  id: string;
  seatNumber: number;
  label: string;
  status: "AVAILABLE" | "LOCKED" | "BOOKED" | string;
};

export type PaymentMethod = "CASH" | "PROMPTPAY" | "BANK_TRANSFER";

export type NotificationSummary = {
  id: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  channel: string;
  deliveryStatus: string;
  data?: Record<string, unknown> | null;
  createdAt: string;
};

export type PaymentSummary = {
  id: string;
  bookingId?: string;
  status: string;
  method: PaymentMethod;
  amount?: number;
  transactionId?: string | null;
  proofImageUrl?: string | null;
  proofReference?: string | null;
  proofSubmittedAt?: string | null;
  paidAt?: string | null;
  reviewNote?: string | null;
  createdAt?: string | null;
};

export type BookingSummary = {
  id: string;
  bookingCode: string;
  status: string;
  passengerName: string;
  passengerPhone: string;
  pickupLocation: string;
  totalPrice: number;
  paymentDueAt?: string | null;
  route: RouteSummary;
  timeslot: TimeslotSummary;
  seats: SeatSummary[];
  payment?: PaymentSummary | null;
};
