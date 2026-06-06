import { format } from "date-fns";
import { CalendarDays, Clock3, MapPin, Phone, Ticket, UserRound } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { SeatMapPanel } from "@/features/bookings/components/SeatMapPanel";
import { useBooking } from "@/features/bookings/hooks/useBooking";
import { useCancelBooking } from "@/features/bookings/hooks/useCancelBooking";
import { useRescheduleBooking } from "@/features/bookings/hooks/useRescheduleBooking";
import { useUpdateBooking } from "@/features/bookings/hooks/useUpdateBooking";
import { bookingStatusClass, canEditBooking, canRescheduleBooking, formatLabel } from "@/features/bookings/utils";
import { useSeatMap } from "@/features/seats/hooks/useSeatMap";
import { useTimeslots } from "@/features/timeslots/hooks/useTimeslots";

type BookingDetailsViewProps = {
  bookingId: string;
};

type DetailForm = {
  passengerName: string;
  passengerPhone: string;
  pickupLocation: string;
};


export function BookingDetailsView({ bookingId }: BookingDetailsViewProps) {
  const navigate = useNavigate();
  const bookingQuery = useBooking(bookingId);
  const updateBooking = useUpdateBooking();
  const cancelBooking = useCancelBooking();
  const rescheduleBooking = useRescheduleBooking();

  const [form, setForm] = useState<DetailForm>({
    passengerName: "",
    passengerPhone: "",
    pickupLocation: "",
  });
  const [rescheduleMode, setRescheduleMode] = useState(false);
  const [rescheduleDate, setRescheduleDate] = useState("");
  const [targetTimeslotId, setTargetTimeslotId] = useState("");
  const [targetSeatIds, setTargetSeatIds] = useState<string[]>([]);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const booking = bookingQuery.data;
  const seatCount = booking?.seats.length ?? 0;
  const routeId = booking?.route.id ?? "";
  const timeslotDate = booking?.timeslot.date ?? "";

  useEffect(() => {
    if (!booking) return;

    setForm({
      passengerName: booking.passengerName,
      passengerPhone: booking.passengerPhone,
      pickupLocation: booking.pickupLocation,
    });
    setRescheduleDate(timeslotDate);
  }, [booking?.id]);

  const timeslotsQuery = useTimeslots({
    routeId,
    date: rescheduleDate || timeslotDate,
  });
  const timeslots = timeslotsQuery.data ?? [];
  const activeTargetTimeslotId = targetTimeslotId || timeslots.find((slot) => slot.id !== booking?.timeslot.id)?.id || "";
  const seatMapQuery = useSeatMap(activeTargetTimeslotId);
  const seatMap = seatMapQuery.data ?? [];

  useEffect(() => {
    if (!rescheduleMode) return;
    if (!timeslots.length) return;

    const differentSlot = timeslots.find((slot) => slot.id !== booking?.timeslot.id);
    const defaultTimeslotId = differentSlot?.id ?? timeslots[0]?.id ?? "";

    setTargetTimeslotId((current) => current || defaultTimeslotId);
    setTargetSeatIds([]);
  }, [booking?.timeslot.id, rescheduleMode, timeslots]);

  const paymentSummary = useMemo(() => {
    if (!booking?.payment) return "No payment record";
    return `${formatLabel(booking.payment.method)} / ${formatLabel(booking.payment.status)}`;
  }, [booking?.payment]);

  async function handleSaveDetails() {
    if (!booking) return;

    setMessage(null);

    try {
      await updateBooking.mutateAsync({
        bookingId: booking.id,
        passengerName: form.passengerName,
        passengerPhone: form.passengerPhone,
        pickupLocation: form.pickupLocation,
      });

      setMessage({
        type: "success",
        text: "Passenger details updated.",
      });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Booking update failed",
      });
    }
  }

  async function handleCancelBooking() {
    if (!booking) return;

    setMessage(null);

    try {
      await cancelBooking.mutateAsync(booking.id);
      setMessage({
        type: "success",
        text: "Booking cancelled.",
      });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Booking cancellation failed",
      });
    }
  }

  async function handleRescheduleBooking() {
    if (!booking) return;

    if (!activeTargetTimeslotId) {
      setMessage({
        type: "error",
        text: "Please choose a new timeslot.",
      });
      return;
    }

    if (targetSeatIds.length !== seatCount) {
      setMessage({
        type: "error",
        text: `Please choose exactly ${seatCount} seat${seatCount === 1 ? "" : "s"}.`,
      });
      return;
    }

    setMessage(null);

    try {
      const nextBooking = await rescheduleBooking.mutateAsync({
        bookingId: booking.id,
        timeslotId: activeTargetTimeslotId,
        seatIds: targetSeatIds,
        idempotencyKey: crypto.randomUUID(),
      });

      navigate(appRoutes.bookingDetails(nextBooking.id));
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Booking reschedule failed",
      });
    }
  }

  if (bookingQuery.isLoading) {
    return (
      <div className="px-4 py-8">
        <div className="h-24 animate-pulse rounded-xl border border-[#d6dcf4] bg-white" />
      </div>
    );
  }

  if (bookingQuery.isError || !booking) {
    return (
      <div className="px-4 py-8">
        <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          Booking details could not be loaded from the backend.
        </p>
      </div>
    );
  }

  const isEditable = canEditBooking(booking.status);
  const canReschedule = canRescheduleBooking(booking.status);
  const isMutating =
    updateBooking.isPending || cancelBooking.isPending || rescheduleBooking.isPending;

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader
        title="Edit Booking"
        subtitle="Update passenger details and reschedule seats"
        showBack
        backHref={appRoutes.myBookings}
      />

      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-4 py-4 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h1 className="mt-1 text-base font-semibold">{booking.bookingCode}</h1>
            <p className="mt-1 text-[11px] text-white/80">
              Update passenger details, reschedule, or cancel when the booking is still active.
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
      </header>

      <section className="mt-3 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">Trip Summary</p>
        <div className="mt-3 grid gap-2 text-[11px] text-[#5564ab]">
          <p className="inline-flex items-center gap-1.5">
            <MapPin className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.route.from} - {booking.route.to}
          </p>
          <p className="inline-flex items-center gap-1.5">
            <CalendarDays className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.timeslot.date}
          </p>
          <p className="inline-flex items-center gap-1.5">
            <Clock3 className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.timeslot.time}
          </p>
          <p className="inline-flex items-center gap-1.5">
            <Ticket className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.seats.map((seat) => seat.label).join(", ")}
          </p>
          <p>Payment: {paymentSummary}</p>
          <p>Total: {booking.totalPrice} THB</p>
          {booking.paymentDueAt ? (
            <p>Payment due: {format(new Date(booking.paymentDueAt), "MMM d, yyyy HH:mm")}</p>
          ) : null}
        </div>
      </section>

      <section className="mt-3 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
          Passenger Details
        </p>
        <div className="mt-3 space-y-3">
          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Name</label>
            <div className="relative">
              <input
                value={form.passengerName}
                onChange={(event) =>
                  setForm((current) => ({ ...current, passengerName: event.target.value }))
                }
                disabled={!isEditable}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 pr-8 text-xs text-[#26368f] outline-none disabled:bg-[#f4f6fd]"
              />
              <UserRound className="pointer-events-none absolute right-2 top-2.5 h-3.5 w-3.5 text-[#91a0dd]" />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Phone</label>
            <div className="relative">
              <input
                value={form.passengerPhone}
                onChange={(event) =>
                  setForm((current) => ({ ...current, passengerPhone: event.target.value }))
                }
                disabled={!isEditable}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 pr-8 text-xs text-[#26368f] outline-none disabled:bg-[#f4f6fd]"
              />
              <Phone className="pointer-events-none absolute right-2 top-2.5 h-3.5 w-3.5 text-[#91a0dd]" />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Pickup Location
            </label>
            <input
              value={form.pickupLocation}
              onChange={(event) =>
                setForm((current) => ({ ...current, pickupLocation: event.target.value }))
              }
              disabled={!isEditable}
              className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none disabled:bg-[#f4f6fd]"
            />
          </div>

          <button
            type="button"
            onClick={handleSaveDetails}
            disabled={!isEditable || isMutating}
            className="h-9 w-full rounded-xl bg-[#3f53c9] text-[12px] font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {updateBooking.isPending ? "Saving..." : "Save Details"}
          </button>
        </div>
      </section>

      <section className="mt-3 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <div className="flex items-center justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">Reschedule</p>
            <p className="mt-1 text-[11px] text-[#6f7cb6]">
              Choose a new date, timeslot, and the same number of seats.
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              setRescheduleMode((current) => !current);
              setTargetSeatIds([]);
            }}
            disabled={!canReschedule || isMutating}
            className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-[10px] font-semibold text-[#3142a5] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {rescheduleMode ? "Hide" : "Show"}
          </button>
        </div>

        {!canReschedule ? (
          <p className="mt-3 rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-xs text-sky-700">
            Reschedule is only available for confirmed or pending-payment bookings.
          </p>
        ) : null}

        {rescheduleMode && canReschedule ? (
          <div className="mt-3 space-y-3">
            <div>
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                New Date
              </label>
              <input
                type="date"
                value={rescheduleDate}
                min={new Date().toISOString().split("T")[0]}
                onChange={(event) => {
                  setRescheduleDate(event.target.value);
                  setTargetTimeslotId("");
                  setTargetSeatIds([]);
                }}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
              />
            </div>

            <div>
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                New Timeslot
              </label>
              <select
                value={activeTargetTimeslotId}
                onChange={(event) => {
                  setTargetTimeslotId(event.target.value);
                  setTargetSeatIds([]);
                }}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
              >
                {timeslots.length === 0 ? <option value="">No timeslots available</option> : null}
                {timeslots.map((timeslot) => (
                  <option key={timeslot.id} value={timeslot.id}>
                    {timeslot.time} ({timeslot.availableSeats} left)
                  </option>
                ))}
              </select>
            </div>

            {seatMapQuery.isLoading ? (
              <div className="space-y-2 py-2">
                <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
                <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
              </div>
            ) : null}

            {seatMapQuery.isError ? (
              <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
                Seat map for reschedule could not be loaded.
              </p>
            ) : null}

            {!seatMapQuery.isLoading && !seatMapQuery.isError && seatMap.length > 0 ? (
              <SeatMapPanel
                seats={seatMap}
                selectedSeatIds={targetSeatIds}
                onSelectionChange={setTargetSeatIds}
                maxSeats={seatCount}
                disabled={isMutating}
              />
            ) : null}

            <button
              type="button"
              onClick={handleRescheduleBooking}
              disabled={isMutating || !activeTargetTimeslotId}
              className="h-9 w-full rounded-xl border border-[#cbd3f1] bg-white text-[12px] font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff] disabled:cursor-not-allowed disabled:opacity-70"
            >
              {rescheduleBooking.isPending ? "Rescheduling..." : "Confirm Reschedule"}
            </button>
          </div>
        ) : null}
      </section>

      {message ? (
        <p
          className={`mt-3 rounded-lg border px-3 py-2 text-xs ${
            message.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-700"
              : "border-amber-200 bg-amber-50 text-amber-700"
          }`}
        >
          {message.text}
        </p>
      ) : null}

      <button
        type="button"
        onClick={handleCancelBooking}
        disabled={!isEditable || isMutating}
        className="mt-4 h-10 w-full rounded-xl border border-rose-200 bg-rose-50 text-sm font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
      >
        {cancelBooking.isPending ? "Cancelling..." : "Cancel Booking"}
      </button>
    </div>
  );
}
