import { format } from "date-fns";
import { Clock3, MapPin, Phone, UserRound } from "lucide-react";
import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useAuth } from "@/features/auth/AuthProvider";
import { useCreateBooking } from "@/features/bookings/hooks/useCreateBooking";
import { SeatMapPanel } from "@/features/bookings/components/SeatMapPanel";
import { useRoute } from "@/features/routes/hooks/useRoute";
import { useSeatMap } from "@/features/seats/hooks/useSeatMap";
import { useTimeslots } from "@/features/timeslots/hooks/useTimeslots";
import type { PaymentMethod } from "@/types/domain";

const PAYMENT_METHOD_OPTIONS: { value: PaymentMethod; label: string }[] = [
  { value: "CASH", label: "Cash" },
  { value: "PROMPTPAY", label: "PromptPay" },
  { value: "BANK_TRANSFER", label: "Transfer" },
];

type BookRouteViewProps = {
  routeId: string;
};

export function BookRouteView({ routeId }: BookRouteViewProps) {
  const { session, isAuthenticated } = useAuth();
  const [searchParams] = useSearchParams();
  const queryDate = searchParams.get("date") || new Date().toISOString().split("T")[0];
  const queryTimeslotId = searchParams.get("timeslotId") || "";

  const [selectedDate, setSelectedDate] = useState(queryDate);
  const [selectedTimeslotId, setSelectedTimeslotId] = useState(queryTimeslotId);
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);
  const [passengerName, setPassengerName] = useState(session?.user.name || "");
  const [passengerPhone, setPassengerPhone] = useState(session?.user.phone || "");
  const [pickupLocation, setPickupLocation] = useState(session?.user.defaultPickupLocation || "");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("CASH");
  const [submitError, setSubmitError] = useState("");
  const [createdBookingCode, setCreatedBookingCode] = useState("");

  const routeQuery = useRoute(routeId);
  const createBooking = useCreateBooking();
  const timeslotsQuery = useTimeslots({
    routeId,
    date: selectedDate,
  });

  const timeslots = timeslotsQuery.data ?? [];
  const activeTimeslotId = selectedTimeslotId || timeslots[0]?.id || "";
  const seatMapQuery = useSeatMap(activeTimeslotId);
  const seats = seatMapQuery.data ?? [];

  const selectedTimeslot = timeslots.find((timeslot) => timeslot.id === activeTimeslotId) || null;
  const totalPrice = useMemo(() => {
    if (!routeQuery.data) return 0;
    return routeQuery.data.price * selectedSeatIds.length;
  }, [routeQuery.data, selectedSeatIds]);

  function handleTimeslotChange(nextTimeslotId: string) {
    setSelectedTimeslotId(nextTimeslotId);
    setSelectedSeatIds([]);
  }

  async function handleCreateBooking() {
    if (!selectedTimeslot || selectedSeatIds.length === 0) return;

    setSubmitError("");

    try {
      const booking = await createBooking.mutateAsync({
        routeId,
        timeslotId: selectedTimeslot.id,
        seatIds: selectedSeatIds,
        passengerName,
        passengerPhone,
        pickupLocation,
        paymentMethod,
        idempotencyKey: crypto.randomUUID(),
      });

      setCreatedBookingCode(booking.bookingCode);
      setSelectedSeatIds([]);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Booking could not be created");
    }
  }

  if (routeQuery.isLoading || timeslotsQuery.isLoading) {
    return (
      <div className="px-4 py-8">
        <div className="h-24 animate-pulse rounded-xl border border-[#d6dcf4] bg-white" />
      </div>
    );
  }

  if (routeQuery.isError || !routeQuery.data) {
    return (
      <div className="px-4 py-8">
        <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          Route details could not be loaded yet. This screen needs backend auth wiring next.
        </p>
      </div>
    );
  }

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader
        title="Choose Seat"
        subtitle="Select time, seats, and passenger details"
        showBack
        backHref={appRoutes.home}
      />

      <header className="rounded-xl border border-[#d6dcf4] bg-white p-3">
        <div className="space-y-1 text-[11px] text-[#4355b9]">
          <p className="inline-flex items-center gap-1.5">
            <MapPin className="h-3.5 w-3.5" />
            {routeQuery.data.from} - {routeQuery.data.to}
          </p>
          <div className="pt-1">
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Date</label>
            <input
              type="date"
              value={selectedDate}
              min={new Date().toISOString().split("T")[0]}
              onChange={(event) => {
                setSelectedDate(event.target.value);
                setSelectedTimeslotId("");
                setSelectedSeatIds([]);
              }}
              className="h-8 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
            />
          </div>
          <p className="inline-flex items-center gap-1.5">
            <Clock3 className="h-3.5 w-3.5" />
            {selectedTimeslot?.time || "-"}
          </p>
        </div>

        {timeslots.length > 0 ? (
          <div className="mt-2">
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Time</label>
            <select
              value={activeTimeslotId}
              onChange={(event) => handleTimeslotChange(event.target.value)}
              className="h-8 w-full rounded-md border border-[#d7dcf3] px-3 text-xs text-[#26368f] outline-none"
            >
              {timeslots.map((timeslot) => (
                <option key={timeslot.id} value={timeslot.id}>
                  {timeslot.time} ({timeslot.availableSeats} left)
                </option>
              ))}
            </select>
          </div>
        ) : (
          <div className="mt-2 rounded-lg border border-dashed border-[#d6dcf4] bg-[#f9faff] px-3 py-2">
            <p className="text-[11px] font-semibold text-[#3041a1]">No departures on this date</p>
            <p className="mt-0.5 text-[10px] text-[#6f7cb6]">
              Select another date to see available timeslots.
            </p>
          </div>
        )}
      </header>

      <div className="mt-3 rounded-xl border border-[#d6dcf4] bg-white p-3">
        {seatMapQuery.isLoading ? (
          <div className="space-y-2 py-2">
            <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
            <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
          </div>
        ) : null}

        {seatMapQuery.isError && (
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            Seat map could not be loaded. The Java backend likely needs LIFF auth before this call will succeed.
          </p>
        )}

        {!seatMapQuery.isLoading && !seatMapQuery.isError && seats.length === 0 && (
          <div className="rounded-lg border border-dashed border-[#d6dcf4] bg-[#f9faff] px-4 py-5 text-center">
            <p className="text-sm font-semibold text-[#3041a1]">Seat map unavailable</p>
            <p className="mt-1 text-xs text-[#6f7cb6]">
              Choose a date and timeslot with configured seats to continue.
            </p>
          </div>
        )}

        {!seatMapQuery.isLoading && !seatMapQuery.isError && seats.length > 0 && (
          <SeatMapPanel seats={seats} selectedSeatIds={selectedSeatIds} onSelectionChange={setSelectedSeatIds} />
        )}
      </div>

      <div className="mt-3 rounded-xl border border-[#d6dcf4] bg-white p-3">
        <p className="text-[10px] font-semibold uppercase text-[#7682bb]">Passenger Details</p>

        {!isAuthenticated ? (
          <p className="mt-2 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            Sign in first so the Java backend can create bookings for your account.
          </p>
        ) : null}

        <div className="mt-3 space-y-3">
          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Name
            </label>
            <div className="relative">
              <input
                value={passengerName}
                onChange={(event) => setPassengerName(event.target.value)}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 pr-8 text-xs text-[#26368f] outline-none"
                placeholder="Passenger name"
              />
              <UserRound className="pointer-events-none absolute right-2 top-2.5 h-3.5 w-3.5 text-[#91a0dd]" />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Phone
            </label>
            <div className="relative">
              <input
                value={passengerPhone}
                onChange={(event) => setPassengerPhone(event.target.value)}
                className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 pr-8 text-xs text-[#26368f] outline-none"
                placeholder="Passenger phone"
              />
              <Phone className="pointer-events-none absolute right-2 top-2.5 h-3.5 w-3.5 text-[#91a0dd]" />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Pickup Location
            </label>
            <input
              value={pickupLocation}
              onChange={(event) => setPickupLocation(event.target.value)}
              className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
              placeholder="Pickup location"
            />
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Payment Method
            </label>
            <div className="grid grid-cols-3 gap-2">
              {PAYMENT_METHOD_OPTIONS.map((method) => (
                <button
                  key={method.value}
                  type="button"
                  onClick={() => setPaymentMethod(method.value as PaymentMethod)}
                  className={`h-8 rounded border text-[10px] font-semibold transition-colors ${
                    paymentMethod === method.value
                      ? "border-[#4f62d3] bg-[#edf1ff] text-[#3041a1]"
                      : "border-[#d7dcf3] bg-white text-[#6f7cb6]"
                  }`}
                >
                  {method.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="mt-3 rounded-xl border border-[#d6dcf4] bg-white p-3">
        <p className="text-[10px] font-semibold uppercase text-[#7682bb]">Trip Summary</p>
        <p className="mt-1 text-[11px] font-semibold text-[#3041a1]">
          {routeQuery.data.from} - {routeQuery.data.to}
        </p>
        <p className="mt-0.5 text-[10px] text-[#6f7cb6]">{selectedDate}</p>
        <p className="mt-0.5 text-[10px] text-[#6f7cb6]">{selectedTimeslot?.time || "-"}</p>
        <p className="mt-0.5 text-[10px] text-[#6f7cb6]">
          {selectedSeatIds.length} seat{selectedSeatIds.length === 1 ? "" : "s"} ({totalPrice} THB)
        </p>
        {selectedTimeslot ? (
          <p className="mt-2 text-[10px] text-[#7c88bf]">
            Departure preview:{" "}
            {format(new Date(`${selectedTimeslot.date}T${selectedTimeslot.time}`), "EEE, MMM d HH:mm")}
          </p>
        ) : null}
      </div>

      {submitError ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          {submitError}
        </p>
      ) : null}

      {createdBookingCode ? (
        <div className="mt-3 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs text-emerald-700">
          <p>Booking created successfully. Code: {createdBookingCode}</p>
        </div>
      ) : null}

      <button
        type="button"
        disabled={
          !isAuthenticated ||
          selectedSeatIds.length === 0 ||
          !selectedTimeslot ||
          !passengerName ||
          !passengerPhone ||
          !pickupLocation ||
          createBooking.isPending
        }
        onClick={handleCreateBooking}
        className="mt-4 h-10 w-full rounded-lg bg-[#3f53c9] text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-70"
      >
        {createBooking.isPending ? "Creating booking..." : "Create booking"}
      </button>
    </div>
  );
}
