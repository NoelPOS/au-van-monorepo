import { apiRequest } from "@/api/client";
import type { PageResponse } from "@/types/api";
import type {
  BookingSummary,
  PaymentSummary,
  PaymentMethod,
  RouteSummary,
  SeatSummary,
  TimeslotSummary,
} from "@/types/domain";

export type CreateBookingInput = {
  routeId: string;
  timeslotId: string;
  seatIds: string[];
  passengerName: string;
  passengerPhone: string;
  pickupLocation: string;
  paymentMethod: PaymentMethod;
  idempotencyKey?: string;
};

export type AdminBookingsQuery = {
  page?: number;
  size?: number;
};

export type UpdateBookingInput = {
  bookingId: string;
  passengerName: string;
  passengerPhone: string;
  pickupLocation: string;
};

export type RescheduleBookingInput = {
  bookingId: string;
  timeslotId: string;
  seatIds: string[];
  idempotencyKey?: string;
};

type BookingDto = {
  id: string;
  bookingCode: string;
  status: string;
  passengerName: string;
  passengerPhone: string;
  pickupLocation: string;
  totalPrice: number | string;
  paymentDueAt?: string | null;
  route: {
    id: string;
    fromLocation: string;
    toLocation: string;
    slug?: string;
    price: number | string;
    durationMinutes?: number | null;
    status?: string;
  };
  timeslot: {
    id: string;
    routeId: string;
    date: string;
    time: string;
    totalSeats: number;
    bookedSeats: number;
    availableSeats: number;
    status?: string;
  };
  seats: Array<{
    id: string;
    seatNumber: number;
    label: string;
    status: string;
  }>;
  payment?: {
    id: string;
    status: string;
    method: PaymentMethod;
    amount?: number | string;
    transactionId?: string | null;
    proofImageUrl?: string | null;
    proofReference?: string | null;
    proofSubmittedAt?: string | null;
    paidAt?: string | null;
    reviewNote?: string | null;
    createdAt?: string | null;
  } | null;
};

function mapRoute(route: BookingDto["route"]): RouteSummary {
  return {
    id: route.id,
    from: route.fromLocation,
    to: route.toLocation,
    slug: route.slug,
    price: Number(route.price),
    durationMinutes: route.durationMinutes,
    status: route.status,
  };
}

function mapTimeslot(timeslot: BookingDto["timeslot"]): TimeslotSummary {
  return {
    id: timeslot.id,
    date: timeslot.date,
    time: timeslot.time,
    totalSeats: timeslot.totalSeats,
    bookedSeats: timeslot.bookedSeats,
    availableSeats: timeslot.availableSeats,
    status: timeslot.status,
  };
}

function mapSeat(seat: BookingDto["seats"][number]): SeatSummary {
  return {
    id: seat.id,
    seatNumber: seat.seatNumber,
    label: seat.label,
    status: seat.status,
  };
}

function mapPayment(payment: NonNullable<BookingDto["payment"]>): PaymentSummary {
  return {
    id: payment.id,
    status: payment.status,
    method: payment.method,
    amount: payment.amount !== undefined ? Number(payment.amount) : undefined,
    transactionId: payment.transactionId,
    proofImageUrl: payment.proofImageUrl,
    proofReference: payment.proofReference,
    proofSubmittedAt: payment.proofSubmittedAt,
    paidAt: payment.paidAt,
    reviewNote: payment.reviewNote,
    createdAt: payment.createdAt,
  };
}

function mapBooking(dto: BookingDto): BookingSummary {
  return {
    id: dto.id,
    bookingCode: dto.bookingCode,
    status: dto.status,
    passengerName: dto.passengerName,
    passengerPhone: dto.passengerPhone,
    pickupLocation: dto.pickupLocation,
    totalPrice: Number(dto.totalPrice),
    paymentDueAt: dto.paymentDueAt,
    route: mapRoute(dto.route),
    timeslot: mapTimeslot(dto.timeslot),
    seats: dto.seats.map(mapSeat),
    payment: dto.payment ? mapPayment(dto.payment) : null,
  };
}

export function createBooking(input: CreateBookingInput) {
  return apiRequest<BookingDto>("/api/liff/bookings", {
    method: "POST",
    body: JSON.stringify({
      ...input,
      sourceChannel: "LIFF",
      idempotencyKey: input.idempotencyKey,
    }),
  }).then(mapBooking);
}

export function getMyBookings() {
  return apiRequest<BookingDto[]>("/api/liff/bookings").then((bookings) =>
    bookings.map(mapBooking)
  );
}

export function getBooking(bookingId: string) {
  return apiRequest<BookingDto>(`/api/liff/bookings/${bookingId}`).then(mapBooking);
}

export function updateBooking(input: UpdateBookingInput) {
  return apiRequest<BookingDto>(`/api/liff/bookings/${input.bookingId}`, {
    method: "PUT",
    body: JSON.stringify({
      passengerName: input.passengerName,
      passengerPhone: input.passengerPhone,
      pickupLocation: input.pickupLocation,
    }),
  }).then(mapBooking);
}

export function cancelBooking(bookingId: string) {
  return apiRequest<void>(`/api/liff/bookings/${bookingId}`, {
    method: "DELETE",
  });
}

export function rescheduleBooking(input: RescheduleBookingInput) {
  return apiRequest<BookingDto>(`/api/liff/bookings/${input.bookingId}/reschedule`, {
    method: "POST",
    body: JSON.stringify({
      timeslotId: input.timeslotId,
      seatIds: input.seatIds,
      idempotencyKey: input.idempotencyKey,
    }),
  }).then(mapBooking);
}

export function getAdminBookings(query: AdminBookingsQuery = {}) {
  return apiRequest<PageResponse<BookingDto>>("/api/admin/bookings", {
    query: {
      page: query.page ?? 0,
      size: query.size ?? 20,
    },
  }).then((page) => ({
    ...page,
    content: page.content.map(mapBooking),
  }));
}

export function cancelAdminBooking(bookingId: string) {
  return apiRequest<void>(`/api/admin/bookings/${bookingId}`, {
    method: "DELETE",
  });
}
