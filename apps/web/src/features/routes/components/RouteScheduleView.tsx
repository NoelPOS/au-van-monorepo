import { addDays, format } from "date-fns";
import { CalendarDays, Ticket } from "lucide-react";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useRoutes } from "@/features/routes/hooks/useRoutes";
import { useTimeslots } from "@/features/timeslots/hooks/useTimeslots";

type QuickDate = {
  value: string;
  label: string;
};

function buildQuickDates(): QuickDate[] {
  const baseDate = new Date();

  return [0, 1, 2].map((offset) => {
    const date = addDays(baseDate, offset);

    return {
      value: date.toISOString().split("T")[0],
      label: offset === 0 ? "Today" : offset === 1 ? "Tomorrow" : format(date, "EEE"),
    };
  });
}

export function RouteScheduleView() {
  const navigate = useNavigate();

  const [selectedRouteId, setSelectedRouteId] = useState("");
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split("T")[0]);
  const [selectedTimeslotId, setSelectedTimeslotId] = useState("");

  const quickDates = useMemo(() => buildQuickDates(), []);

  const routesQuery = useRoutes();
  const routes = routesQuery.data ?? [];
  const activeRouteId = selectedRouteId || routes[0]?.id || "";

  const timeslotsQuery = useTimeslots({
    routeId: activeRouteId,
    date: selectedDate,
  });

  const selectedRoute = routes.find((route) => route.id === activeRouteId);
  const timeslots = timeslotsQuery.data ?? [];

  function handleRouteChange(nextRouteId: string) {
    setSelectedRouteId(nextRouteId);
    setSelectedTimeslotId("");
  }

  function handleContinue() {
    navigate(
      `${appRoutes.book(activeRouteId)}?date=${selectedDate}&timeslotId=${selectedTimeslotId}`
    );
  }

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader
        title="Choose Your Destination"
        subtitle="Pick route, date, and timeslot in one flow"
      />

      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-4 py-4 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <h1 className="mt-1 text-base font-semibold">Choose your trip</h1>
        <p className="mt-1 text-[11px] text-white/80">Pick destination, date, and slot in under a minute.</p>
      </header>

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <div>
          <label className="mb-1 block text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            Route
          </label>
          <select
            value={activeRouteId}
            onChange={(event) => handleRouteChange(event.target.value)}
            className="h-10 w-full rounded-lg border border-[#d9def4] px-3 text-xs text-[#22339a] outline-none"
          >
            {routes.length === 0 && <option value="">No routes yet</option>}
            {routes.map((route) => (
              <option key={route.id} value={route.id}>
                {route.from} - {route.to}
              </option>
            ))}
          </select>
        </div>

        <div className="mt-3">
          <label className="mb-1 block text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            Date
          </label>
          <div className="grid grid-cols-3 gap-2">
            {quickDates.map((item) => {
              const isActive = selectedDate === item.value;

              return (
                <button
                  key={item.value}
                  type="button"
                  onClick={() => setSelectedDate(item.value)}
                  className={`h-8 rounded-md border text-[10px] font-semibold transition-colors ${
                    isActive
                      ? "border-[#4f62d3] bg-[#eaf0ff] text-[#2d40a6]"
                      : "border-[#d7ddf4] bg-white text-[#6f7cb6] hover:bg-[#f5f7ff]"
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </div>

          <input
            type="date"
            value={selectedDate}
            min={new Date().toISOString().split("T")[0]}
            onChange={(event) => setSelectedDate(event.target.value)}
            className="mt-2 h-10 w-full rounded-lg border border-[#d9def4] px-3 text-xs text-[#22339a] outline-none"
          />
        </div>
      </section>

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <div className="mb-2 flex items-center justify-between">
          <p className="text-[11px] font-semibold text-[#22339a]">Available Timeslots</p>
          <span className="text-[10px] text-[#7c88bf]">{format(new Date(selectedDate), "EEE, MMM d")}</span>
        </div>

        {routesQuery.isLoading || timeslotsQuery.isLoading ? (
          <div className="space-y-2 py-2">
            <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
            <div className="h-14 animate-pulse rounded-xl border border-[#dbe2fb] bg-[#f4f7ff]" />
          </div>
        ) : null}

        {(routesQuery.isError || timeslotsQuery.isError) && (
          <p className="rounded-md border border-amber-200 bg-amber-50 px-2 py-1.5 text-[11px] text-amber-700">
            Failed to load routes or timeslots. Please try again.
          </p>
        )}

        {!timeslotsQuery.isLoading && !timeslotsQuery.isError && timeslots.length === 0 && (
          <div className="rounded-lg border border-dashed border-[#cad3f1] bg-[#fbfcff] px-3 py-7 text-center">
            <CalendarDays className="mx-auto mb-2 h-5 w-5 text-[#98a5da]" />
            <p className="text-xs text-[#6f7cb6]">No slots on this day. Try another date.</p>
          </div>
        )}

        {!timeslotsQuery.isLoading && !timeslotsQuery.isError && timeslots.length > 0 && (
          <div className="grid grid-cols-2 gap-2">
            {timeslots.map((timeslot) => {
              const availableSeats = timeslot.totalSeats - timeslot.bookedSeats;
              const isActive = selectedTimeslotId === timeslot.id;

              return (
                <button
                  key={timeslot.id}
                  type="button"
                  onClick={() => setSelectedTimeslotId(timeslot.id)}
                  className={`rounded-xl border px-2 py-2 text-left transition-colors ${
                    isActive
                      ? "border-[#445bd0] bg-[#ecf1ff]"
                      : "border-[#e1e6f8] bg-[#fafbff] hover:border-[#c5cef0]"
                  }`}
                >
                  <p className="text-[12px] font-semibold text-[#2f3f9f]">{timeslot.time}</p>
                  <div className="mt-1 flex items-center justify-between text-[10px] text-[#6f7cb6]">
                    <span className="inline-flex items-center gap-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-[#57c088]" />
                      {availableSeats} seats
                    </span>
                    <span>{selectedRoute?.price ?? 0} THB</span>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </section>

      <button
        type="button"
        className="mt-4 flex h-11 w-full items-center justify-center gap-1.5 rounded-xl bg-[#3f53c9] text-xs font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-60"
        disabled={!activeRouteId || !selectedTimeslotId}
        onClick={handleContinue}
      >
        <Ticket className="h-4 w-4" />
        Continue to Seat Selection
      </button>

      <p className="mt-2 text-center text-[10px] text-[#7d88bf]">
        Route:{" "}
        <span className="font-semibold text-[#5666b8]">
          {selectedRoute ? `${selectedRoute.from} to ${selectedRoute.to}` : "-"}
        </span>
      </p>
    </div>
  );
}
