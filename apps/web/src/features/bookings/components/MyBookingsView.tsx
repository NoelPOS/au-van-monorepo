import { format } from "date-fns";
import { CalendarDays, Clock3, CreditCard, MapPin, ReceiptText, Ticket } from "lucide-react";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useMyBookings } from "@/features/bookings/hooks/useMyBookings";
import { bookingStatusClass, formatLabel, isCancelledBooking, paymentStatusClass } from "@/features/bookings/utils";
import type { BookingSummary } from "@/types/domain";

type BookingTab = "active" | "cancelled";

function needsPaymentProof(booking: BookingSummary) {
  if (!booking.payment) return false;
  if (booking.payment.method === "CASH") return false;

  return booking.payment.status === "PENDING";
}

function BookingCard({ booking }: { booking: BookingSummary }) {
  return (
    <article className="rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">
            {booking.bookingCode}
          </p>
          <p className="mt-1 text-sm font-semibold text-[#2f3f9f]">
            {booking.route.from} - {booking.route.to}
          </p>
        </div>
        <span
          className={`rounded-full border px-2 py-1 text-[10px] font-semibold ${bookingStatusClass(
            booking.status
          )}`}
        >
          {formatLabel(booking.status)}
        </span>
      </div>

      <div className="mt-3 grid gap-2 text-[11px] text-[#6f7cb6]">
        <p className="inline-flex items-center gap-1.5">
          <CalendarDays className="h-3.5 w-3.5 text-[#8f9bd7]" />
          {format(new Date(booking.timeslot.date), "EEE, MMM d")}
        </p>
        <p className="inline-flex items-center gap-1.5">
          <Clock3 className="h-3.5 w-3.5 text-[#8f9bd7]" />
          {booking.timeslot.time}
        </p>
        <p className="inline-flex items-center gap-1.5">
          <Ticket className="h-3.5 w-3.5 text-[#8f9bd7]" />
          {booking.seats.map((seat) => seat.label).join(", ")}
        </p>
        <p className="inline-flex items-center gap-1.5">
          <MapPin className="h-3.5 w-3.5 text-[#8f9bd7]" />
          {booking.pickupLocation}
        </p>
      </div>

      <div className="mt-3 rounded-xl bg-[#f7f8fd] px-3 py-2">
        <div className="flex items-center justify-between gap-3">
          <p className="inline-flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            <CreditCard className="h-3.5 w-3.5" />
            Payment
          </p>
          <p className="text-xs font-semibold text-[#3041a1]">{booking.totalPrice} THB</p>
        </div>

        {booking.payment ? (
          <div className="mt-2 space-y-1">
            <p className="text-[11px] text-[#5564ab]">Method: {formatLabel(booking.payment.method)}</p>
            <p className={`text-[11px] font-semibold ${paymentStatusClass(booking.payment.status)}`}>
              Status: {formatLabel(booking.payment.status)}
            </p>
          </div>
        ) : (
          <p className="mt-2 text-[11px] text-[#5564ab]">Payment record will appear after booking processing.</p>
        )}

        {booking.paymentDueAt ? (
          <p className="mt-2 text-[10px] text-[#7b87be]">
            Due by {format(new Date(booking.paymentDueAt), "MMM d, HH:mm")}
          </p>
        ) : null}
      </div>

      {needsPaymentProof(booking) ? (
        <div className="mt-3 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-[11px] text-amber-700">
          <p className="inline-flex items-center gap-1.5 font-semibold">
            <ReceiptText className="h-3.5 w-3.5" />
            Payment proof is required for this booking.
          </p>
          <p className="mt-1">Upload your slip so admin can review and confirm the payment.</p>
          <Link
            to={appRoutes.payment(booking.id)}
            className="mt-3 inline-flex h-9 items-center justify-center rounded-xl bg-[#3f53c9] px-3 text-xs font-semibold text-white transition-colors hover:bg-[#3447b4]"
          >
            Upload proof
          </Link>
        </div>
      ) : null}

      <Link
        to={appRoutes.bookingDetails(booking.id)}
        className="mt-3 inline-flex h-9 items-center justify-center rounded-xl border border-[#cbd3f1] bg-white px-3 text-xs font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff]"
      >
        View Details
      </Link>
    </article>
  );
}

export function MyBookingsView() {
  const [activeTab, setActiveTab] = useState<BookingTab>("active");
  const bookingsQuery = useMyBookings();
  const bookings = bookingsQuery.data ?? [];

  const filteredBookings = useMemo(() => {
    return bookings.filter((booking) =>
      activeTab === "cancelled"
        ? isCancelledBooking(booking.status)
        : !isCancelledBooking(booking.status)
    );
  }, [activeTab, bookings]);

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader title="My Bookings" subtitle="Track active and cancelled trips" />

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-2 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <div className="grid grid-cols-2 gap-2">
          {[
            { value: "active", label: "Active" },
            { value: "cancelled", label: "Cancelled" },
          ].map((tab) => {
            const isActive = activeTab === tab.value;

            return (
              <button
                key={tab.value}
                type="button"
                onClick={() => setActiveTab(tab.value as BookingTab)}
                className={`h-9 rounded-xl text-xs font-semibold transition-colors ${
                  isActive
                    ? "bg-[#4f62d3] text-white"
                    : "bg-[#eef1fa] text-[#6571a9] hover:bg-[#e5eaf8]"
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </section>

      {bookingsQuery.isLoading ? (
        <div className="mt-4 space-y-3">
          <div className="h-28 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
          <div className="h-28 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
        </div>
      ) : null}

      {bookingsQuery.isError ? (
        <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-xs text-amber-700">
          Your bookings could not be loaded.
        </div>
      ) : null}

      {!bookingsQuery.isLoading && !bookingsQuery.isError && filteredBookings.length === 0 ? (
        <div className="mt-4 rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
          <p className="text-sm font-semibold text-[#3041a1]">No bookings in this tab yet</p>
          <p className="mt-1 text-xs text-[#6f7cb6]">
            Once you create a trip, it will show up here with payment and trip status.
          </p>
          <Link
            to={appRoutes.home}
            className="mt-4 inline-flex h-10 items-center justify-center rounded-xl bg-[#3f53c9] px-4 text-xs font-semibold text-white transition-colors hover:bg-[#3447b4]"
          >
            Book a trip
          </Link>
        </div>
      ) : null}

      {!bookingsQuery.isLoading && !bookingsQuery.isError && filteredBookings.length > 0 ? (
        <div className="mt-4 space-y-3">
          {filteredBookings.map((booking) => (
            <BookingCard key={booking.id} booking={booking} />
          ))}
        </div>
      ) : null}
    </div>
  );
}
