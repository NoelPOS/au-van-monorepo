import { format } from "date-fns";
import { Ban, CalendarDays, Clock3, MapPin, ReceiptText, Ticket } from "lucide-react";
import { useMemo, useState } from "react";
import { useAdminBookings } from "@/features/bookings/hooks/useAdminBookings";
import { useCancelAdminBooking } from "@/features/bookings/hooks/useCancelAdminBooking";
import { bookingStatusClass, formatLabel, isActiveMutableBooking } from "@/features/bookings/utils";
import type { BookingSummary } from "@/types/domain";

type BookingTab = "active" | "all" | "cancelled";

function formatDateTime(dateValue: string, timeValue: string) {
  const date = new Date(`${dateValue}T${timeValue}`);
  if (Number.isNaN(date.getTime())) {
    return `${dateValue} ${timeValue}`;
  }
  return format(date, "MMM d, yyyy HH:mm");
}

type BookingDetailPanelProps = {
  booking: BookingSummary;
  onClose: () => void;
};

function BookingDetailPanel({ booking, onClose }: BookingDetailPanelProps) {
  const cancelBooking = useCancelAdminBooking();
  const [submitError, setSubmitError] = useState("");
  const canCancel = isActiveMutableBooking(booking.status);

  async function handleCancel() {
    setSubmitError("");

    try {
      await cancelBooking.mutateAsync(booking.id);
      onClose();
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Booking cancellation failed");
    }
  }

  return (
    <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">Booking Details</p>
          <h2 className="mt-1 text-lg font-semibold text-[#22339a]">{booking.bookingCode}</h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-xs font-semibold text-[#6875b0]"
        >
          Close
        </button>
      </div>

      <div className="mt-4 rounded-xl bg-[#f7f8fd] p-3 text-sm text-[#4c5ca7]">
        <p><span className="font-semibold text-[#22339a]">Route:</span> {booking.route.from} - {booking.route.to}</p>
        <p><span className="font-semibold text-[#22339a]">Departure:</span> {formatDateTime(booking.timeslot.date, booking.timeslot.time)}</p>
        <p><span className="font-semibold text-[#22339a]">Passenger:</span> {booking.passengerName}</p>
        <p><span className="font-semibold text-[#22339a]">Phone:</span> {booking.passengerPhone}</p>
        <p><span className="font-semibold text-[#22339a]">Pickup:</span> {booking.pickupLocation}</p>
        <p><span className="font-semibold text-[#22339a]">Seats:</span> {booking.seats.map((seat) => seat.label).join(", ")}</p>
        <p><span className="font-semibold text-[#22339a]">Passengers:</span> {booking.seats.length}</p>
        <p><span className="font-semibold text-[#22339a]">Total:</span> {booking.totalPrice} THB</p>
        <p><span className="font-semibold text-[#22339a]">Status:</span> {formatLabel(booking.status)}</p>
        <p><span className="font-semibold text-[#22339a]">Payment:</span> {booking.payment ? `${formatLabel(booking.payment.method)} / ${formatLabel(booking.payment.status)}` : "No payment record"}</p>
      </div>

      {booking.paymentDueAt ? (
        <p className="mt-3 rounded-lg border border-[#dbe1f7] bg-[#f8f9ff] px-3 py-2 text-xs text-[#5a68aa]">
          Payment due by {format(new Date(booking.paymentDueAt), "MMM d, yyyy HH:mm")}
        </p>
      ) : null}

      {submitError ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          {submitError}
        </p>
      ) : null}

      {!canCancel ? (
        <p className="mt-3 rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-xs text-sky-700">
          This booking can no longer be cancelled from admin because it is already {formatLabel(booking.status)}.
        </p>
      ) : null}

      <div className="mt-4 flex justify-end">
        <button
          type="button"
          onClick={handleCancel}
          disabled={!canCancel || cancelBooking.isPending}
          className="inline-flex h-10 items-center justify-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-4 text-sm font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
        >
          <Ban className="h-4 w-4" />
          {cancelBooking.isPending ? "Cancelling..." : "Cancel Booking"}
        </button>
      </div>
    </div>
  );
}

export function AdminBookingsView() {
  const [page, setPage] = useState(0);
  const [activeTab, setActiveTab] = useState<BookingTab>("active");
  const [selectedBooking, setSelectedBooking] = useState<BookingSummary | null>(null);
  const bookingsQuery = useAdminBookings({ page, size: 20 });

  const pageData = bookingsQuery.data;
  const bookings = pageData?.content ?? [];

  const visibleBookings = useMemo(() => {
    switch (activeTab) {
      case "cancelled":
        return bookings.filter((booking) => booking.status === "CANCELLED");
      case "all":
        return bookings;
      default:
        return bookings.filter((booking) => isActiveMutableBooking(booking.status));
    }
  }, [activeTab, bookings]);

  const counts = useMemo(
    () => ({
      active: bookings.filter((booking) => isActiveMutableBooking(booking.status)).length,
      all: bookings.length,
      cancelled: bookings.filter((booking) => booking.status === "CANCELLED").length,
    }),
    [bookings]
  );

  return (
    <section>
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Bookings</p>
        <h1 className="mt-1 text-xl font-semibold">Monitor and manage booking records</h1>
        <p className="mt-1 text-sm text-white/80">
          This screen uses the current Java admin booking API: paged list, booking details, and admin cancellation.
        </p>
      </header>

      <div className="mt-5 grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
        <div>
          <div className="mb-4 flex flex-wrap items-center gap-2">
            {[
              { value: "active", label: `Active (${counts.active})` },
              { value: "all", label: `All on Page (${counts.all})` },
              { value: "cancelled", label: `Cancelled (${counts.cancelled})` },
            ].map((tab) => {
              const isActive = activeTab === tab.value;

              return (
                <button
                  key={tab.value}
                  type="button"
                  onClick={() => setActiveTab(tab.value as BookingTab)}
                  className={`rounded-full px-4 py-2 text-sm font-semibold transition-colors ${
                    isActive
                      ? "bg-[#445bd0] text-white"
                      : "bg-white text-[#6674b0] hover:bg-[#eef2fb]"
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>

          {bookingsQuery.isLoading ? (
            <div className="space-y-3">
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
            </div>
          ) : null}

          {bookingsQuery.isError ? (
            <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-700">
              Admin bookings could not be loaded from the backend.
            </div>
          ) : null}

          {!bookingsQuery.isLoading && !bookingsQuery.isError && visibleBookings.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <Ticket className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">No bookings in this view</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                Change tabs or wait for new bookings to be created.
              </p>
            </div>
          ) : null}

          {!bookingsQuery.isLoading && !bookingsQuery.isError && visibleBookings.length > 0 ? (
            <div className="space-y-3">
              {visibleBookings.map((booking) => (
                <button
                  key={booking.id}
                  type="button"
                  onClick={() => setSelectedBooking(booking)}
                  className="w-full rounded-2xl border border-[#d6dcf4] bg-white p-4 text-left shadow-[0_8px_20px_rgba(57,85,194,0.06)] transition-colors hover:border-[#c1cbee]"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">
                        {booking.bookingCode}
                      </p>
                      <p className="mt-1 text-sm font-semibold text-[#22339a]">
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

                  <div className="mt-3 grid gap-1 text-[11px] text-[#5564ab]">
                    <p className="inline-flex items-center gap-1.5">
                      <CalendarDays className="h-3.5 w-3.5 text-[#8f9bd7]" />
                      {booking.timeslot.date}
                    </p>
                    <p className="inline-flex items-center gap-1.5">
                      <Clock3 className="h-3.5 w-3.5 text-[#8f9bd7]" />
                      {booking.timeslot.time}
                    </p>
                    <p className="inline-flex items-center gap-1.5">
                      <MapPin className="h-3.5 w-3.5 text-[#8f9bd7]" />
                      {booking.pickupLocation}
                    </p>
                    <p className="inline-flex items-center gap-1.5">
                      <Ticket className="h-3.5 w-3.5 text-[#8f9bd7]" />
                      {booking.seats.map((seat) => seat.label).join(", ")} ({booking.totalPrice} THB)
                    </p>
                  </div>

                  <div className="mt-3 inline-flex items-center gap-1.5 text-xs font-semibold text-[#4054c5]">
                    <ReceiptText className="h-3.5 w-3.5" />
                    {isActiveMutableBooking(booking.status) ? "Open booking actions" : "View booking details"}
                  </div>
                </button>
              ))}
            </div>
          ) : null}

          {!bookingsQuery.isLoading && !bookingsQuery.isError && (pageData?.totalPages ?? 0) > 1 ? (
            <div className="mt-4 flex items-center justify-between rounded-2xl border border-[#d6dcf4] bg-white px-4 py-3">
              <button
                type="button"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                disabled={page === 0}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Previous
              </button>
              <p className="text-sm text-[#6f7cb6]">
                Page {page + 1} of {pageData?.totalPages ?? 1}
              </p>
              <button
                type="button"
                onClick={() =>
                  setPage((current) =>
                    Math.min((pageData?.totalPages ?? 1) - 1, current + 1)
                  )
                }
                disabled={page >= (pageData?.totalPages ?? 1) - 1}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Next
              </button>
            </div>
          ) : null}
        </div>

        <div>
          {selectedBooking ? (
            <BookingDetailPanel booking={selectedBooking} onClose={() => setSelectedBooking(null)} />
          ) : (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-12 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <ReceiptText className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">Choose a booking to inspect</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                The detail panel will show route, passenger, seats, payment status, and admin cancellation.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
