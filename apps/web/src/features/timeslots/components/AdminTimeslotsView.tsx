import { addDays } from "date-fns";
import { CalendarDays, Clock3, Layers3, PencilLine, Plus, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { useAdminRoutes } from "@/features/routes/hooks/useAdminRoutes";
import { useAdminTimeslots } from "@/features/timeslots/hooks/useAdminTimeslots";
import { useBulkCreateAdminTimeslots } from "@/features/timeslots/hooks/useBulkCreateAdminTimeslots";
import { useCancelAdminTimeslot } from "@/features/timeslots/hooks/useCancelAdminTimeslot";
import { useUpdateAdminTimeslot } from "@/features/timeslots/hooks/useUpdateAdminTimeslot";
import type { AdminTimeslotSummary } from "@/features/timeslots/api";

type EditTimeslotForm = {
  date: string;
  time: string;
  status: "ACTIVE" | "CANCELLED";
};

type BulkForm = {
  routeId: string;
  dateFrom: string;
  dateTo: string;
  daysOfWeek: number[];
  times: string[];
  totalSeats: string;
};

const emptyEditForm: EditTimeslotForm = {
  date: "",
  time: "",
  status: "ACTIVE",
};

const defaultBulkForm: BulkForm = {
  routeId: "",
  dateFrom: new Date().toISOString().split("T")[0],
  dateTo: addDays(new Date(), 6).toISOString().split("T")[0],
  daysOfWeek: [1, 2, 3, 4, 5],
  times: ["07:00", "08:00", "09:00", "13:00"],
  totalSeats: "12",
};

const PRESET_TIMES = ["07:00", "08:00", "09:00", "10:00", "13:00", "16:00", "18:00"];

function timeslotStatusClass(status?: string) {
  if (status === "CANCELLED") {
    return "border-rose-200 bg-rose-50 text-rose-700";
  }

  return "border-emerald-200 bg-emerald-50 text-emerald-700";
}

function toEditForm(timeslot: AdminTimeslotSummary): EditTimeslotForm {
  return {
    date: timeslot.date,
    time: timeslot.time,
    status: timeslot.status === "CANCELLED" ? "CANCELLED" : "ACTIVE",
  };
}

function countMatchingDays(dateFrom: string, dateTo: string, daysOfWeek: number[]) {
  if (!dateFrom || !dateTo || dateFrom > dateTo || daysOfWeek.length === 0) return 0;

  let count = 0;
  let cursor = new Date(`${dateFrom}T00:00:00`);
  const end = new Date(`${dateTo}T00:00:00`);

  while (cursor <= end) {
    const day = cursor.getDay() === 0 ? 7 : cursor.getDay();
    if (daysOfWeek.includes(day)) {
      count += 1;
    }
    cursor = addDays(cursor, 1);
  }

  return count;
}

export function AdminTimeslotsView() {
  const routesQuery = useAdminRoutes();
  const timeslotsQuery = useAdminTimeslots(0, 200);
  const bulkCreateTimeslots = useBulkCreateAdminTimeslots();
  const updateTimeslot = useUpdateAdminTimeslot();
  const cancelTimeslot = useCancelAdminTimeslot();

  const routes = routesQuery.data?.filter((route) => route.status !== "INACTIVE") ?? [];
  const [selectedRouteId, setSelectedRouteId] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [bulkForm, setBulkForm] = useState<BulkForm>(defaultBulkForm);
  const [customTime, setCustomTime] = useState("");
  const [selectedTimeslot, setSelectedTimeslot] = useState<AdminTimeslotSummary | null>(null);
  const [editForm, setEditForm] = useState<EditTimeslotForm>(emptyEditForm);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const timeslots = timeslotsQuery.data ?? [];
  const activeRouteId = selectedRouteId || routes[0]?.id || "";

  const filteredTimeslots = useMemo(() => {
    return timeslots
      .filter((timeslot) => (activeRouteId ? timeslot.routeId === activeRouteId : true))
      .filter((timeslot) => (selectedDate ? timeslot.date === selectedDate : true))
      .sort((left, right) => `${right.date} ${right.time}`.localeCompare(`${left.date} ${left.time}`));
  }, [activeRouteId, selectedDate, timeslots]);

  const bulkSlotEstimate =
    countMatchingDays(bulkForm.dateFrom, bulkForm.dateTo, bulkForm.daysOfWeek) * bulkForm.times.length;

  function resetBulkForm() {
    setBulkForm((current) => ({ ...defaultBulkForm, routeId: current.routeId || activeRouteId || "" }));
    setCustomTime("");
  }

  function addCustomTime() {
    if (!customTime || bulkForm.times.includes(customTime)) return;
    setBulkForm((current) => ({ ...current, times: [...current.times, customTime].sort() }));
    setCustomTime("");
  }

  function selectTimeslot(timeslot: AdminTimeslotSummary) {
    setSelectedTimeslot(timeslot);
    setEditForm(toEditForm(timeslot));
    setMessage(null);
  }

  async function handleBulkCreate() {
    setMessage(null);

    try {
      const created = await bulkCreateTimeslots.mutateAsync({
        routeId: bulkForm.routeId || activeRouteId,
        dateFrom: bulkForm.dateFrom,
        dateTo: bulkForm.dateTo,
        daysOfWeek: bulkForm.daysOfWeek,
        times: [...bulkForm.times].sort(),
        totalSeats: Number(bulkForm.totalSeats),
      });

      setMessage({ type: "success", text: `${created.length} timeslot(s) created.` });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Bulk timeslot creation failed",
      });
    }
  }

  async function handleUpdateTimeslot() {
    if (!selectedTimeslot) return;

    setMessage(null);

    try {
      await updateTimeslot.mutateAsync({
        timeslotId: selectedTimeslot.id,
        date: editForm.date,
        time: editForm.time,
        status: editForm.status,
      });

      setMessage({ type: "success", text: "Timeslot updated." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Timeslot update failed",
      });
    }
  }

  async function handleCancelTimeslot(timeslot: AdminTimeslotSummary) {
    setMessage(null);

    try {
      await cancelTimeslot.mutateAsync(timeslot.id);
      if (selectedTimeslot?.id === timeslot.id) {
        setSelectedTimeslot(null);
        setEditForm(emptyEditForm);
      }
      setMessage({ type: "success", text: "Timeslot cancelled." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Timeslot cancellation failed",
      });
    }
  }

  const isBusy = bulkCreateTimeslots.isPending || updateTimeslot.isPending || cancelTimeslot.isPending;

  return (
    <section>
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Timeslots</p>
        <h1 className="mt-1 text-xl font-semibold">Plan departures and manage schedules</h1>
        <p className="mt-1 text-sm text-white/80">
          Generate a weekly schedule batch or edit individual slots. Seat count is fixed at creation time to stay aligned with current backend behavior.
        </p>
      </header>

      {message ? (
        <p
          className={`mt-4 rounded-lg border px-3 py-2 text-sm ${
            message.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-700"
              : "border-amber-200 bg-amber-50 text-amber-700"
          }`}
        >
          {message.text}
        </p>
      ) : null}

      <div className="mt-5 grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
        <div className="space-y-4">
          <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
            <div className="grid gap-3 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Route</label>
                <select
                  value={activeRouteId}
                  onChange={(event) => {
                    setSelectedRouteId(event.target.value);
                    setBulkForm((current) => ({ ...current, routeId: event.target.value }));
                  }}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                >
                  {routes.map((route) => (
                    <option key={route.id} value={route.id}>
                      {route.from} - {route.to}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Filter by Date
                </label>
                <input
                  type="date"
                  value={selectedDate}
                  onChange={(event) => setSelectedDate(event.target.value)}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
            <div className="mb-3 flex items-center justify-between gap-3">
              <div>
                <p className="text-sm font-semibold text-[#22339a]">Generate Schedule</p>
                <p className="text-xs text-[#6f7cb6]">Create multiple departures across a date range.</p>
              </div>
              <button
                type="button"
                onClick={resetBulkForm}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-[10px] font-semibold text-[#3142a5]"
              >
                Reset
              </button>
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  From Date
                </label>
                <input
                  type="date"
                  value={bulkForm.dateFrom}
                  onChange={(event) => setBulkForm((current) => ({ ...current, dateFrom: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  To Date
                </label>
                <input
                  type="date"
                  value={bulkForm.dateTo}
                  min={bulkForm.dateFrom}
                  onChange={(event) => setBulkForm((current) => ({ ...current, dateTo: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>
            </div>

            <div className="mt-3">
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                Days of Week
              </label>
              <div className="flex flex-wrap gap-2">
                {[
                  { value: 1, label: "Mon" },
                  { value: 2, label: "Tue" },
                  { value: 3, label: "Wed" },
                  { value: 4, label: "Thu" },
                  { value: 5, label: "Fri" },
                  { value: 6, label: "Sat" },
                  { value: 7, label: "Sun" },
                ].map((day) => {
                  const isActive = bulkForm.daysOfWeek.includes(day.value);

                  return (
                    <button
                      key={day.value}
                      type="button"
                      onClick={() =>
                        setBulkForm((current) => ({
                          ...current,
                          daysOfWeek: current.daysOfWeek.includes(day.value)
                            ? current.daysOfWeek.filter((value) => value !== day.value)
                            : [...current.daysOfWeek, day.value].sort(),
                        }))
                      }
                      className={`rounded-full px-3 py-1.5 text-xs font-semibold transition-colors ${
                        isActive
                          ? "bg-[#445bd0] text-white"
                          : "bg-[#eef1fa] text-[#6674b0] hover:bg-[#e4e9f8]"
                      }`}
                    >
                      {day.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="mt-3">
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                Departure Times
              </label>
              <div className="flex flex-wrap gap-2">
                {PRESET_TIMES.map((time) => {
                  const isActive = bulkForm.times.includes(time);

                  return (
                    <button
                      key={time}
                      type="button"
                      onClick={() =>
                        setBulkForm((current) => ({
                          ...current,
                          times: current.times.includes(time)
                            ? current.times.filter((value) => value !== time)
                            : [...current.times, time].sort(),
                        }))
                      }
                      className={`rounded-full px-3 py-1.5 text-xs font-semibold transition-colors ${
                        isActive
                          ? "bg-[#445bd0] text-white"
                          : "bg-[#eef1fa] text-[#6674b0] hover:bg-[#e4e9f8]"
                      }`}
                    >
                      {time}
                    </button>
                  );
                })}
                {bulkForm.times
                  .filter((time) => !PRESET_TIMES.includes(time))
                  .map((time) => (
                    <button
                      key={time}
                      type="button"
                      onClick={() =>
                        setBulkForm((current) => ({
                          ...current,
                          times: current.times.filter((value) => value !== time),
                        }))
                      }
                      className="rounded-full bg-[#445bd0] px-3 py-1.5 text-xs font-semibold text-white transition-colors hover:bg-[#3447b4]"
                    >
                      {time} ×
                    </button>
                  ))}
              </div>

              <div className="mt-2 flex gap-2">
                <input
                  type="time"
                  value={customTime}
                  onChange={(event) => setCustomTime(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter") addCustomTime();
                  }}
                  className="h-9 flex-1 rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                  placeholder="Custom time"
                />
                <button
                  type="button"
                  onClick={addCustomTime}
                  disabled={!customTime || bulkForm.times.includes(customTime)}
                  className="inline-flex h-9 items-center gap-1 rounded-xl border border-[#cbd3f1] bg-white px-3 text-xs font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff] disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <Plus className="h-3.5 w-3.5" />
                  Add
                </button>
              </div>
            </div>

            <div className="mt-3 w-full max-w-[180px]">
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                Total Seats
              </label>
              <input
                type="number"
                min="1"
                max="50"
                value={bulkForm.totalSeats}
                onChange={(event) =>
                  setBulkForm((current) => ({ ...current, totalSeats: event.target.value }))
                }
                className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
              />
            </div>

            <div className="mt-4 rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] px-3 py-2 text-sm text-[#5564ab]">
              Estimated generation: <span className="font-semibold">{bulkSlotEstimate}</span> timeslot(s)
            </div>

            <button
              type="button"
              onClick={handleBulkCreate}
              disabled={
                isBusy ||
                !activeRouteId ||
                !bulkForm.dateFrom ||
                !bulkForm.dateTo ||
                bulkForm.dateFrom > bulkForm.dateTo ||
                bulkForm.daysOfWeek.length === 0 ||
                bulkForm.times.length === 0 ||
                Number(bulkForm.totalSeats) < 1 ||
                Number(bulkForm.totalSeats) > 50
              }
              className="mt-4 inline-flex h-10 w-full items-center justify-center gap-1.5 rounded-xl border border-[#cbd3f1] bg-white px-4 text-sm font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff] disabled:cursor-not-allowed disabled:opacity-70"
            >
              <Layers3 className="h-4 w-4" />
              {bulkCreateTimeslots.isPending ? "Generating..." : "Generate Schedule"}
            </button>
          </div>

          <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
            <div className="mb-3 flex items-center justify-between gap-3">
              <div>
                <p className="text-sm font-semibold text-[#22339a]">Timeslots</p>
                <p className="text-xs text-[#6f7cb6]">
                  Showing {filteredTimeslots.length} for the current route selection
                </p>
              </div>
            </div>

            {timeslotsQuery.isLoading ? (
              <div className="space-y-3">
                <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
                <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
              </div>
            ) : null}

            {timeslotsQuery.isError ? (
              <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-700">
                Admin timeslots could not be loaded from the backend.
              </div>
            ) : null}

            {!timeslotsQuery.isLoading && !timeslotsQuery.isError && filteredTimeslots.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
                <CalendarDays className="mx-auto h-5 w-5 text-[#98a5da]" />
                <p className="mt-2 text-sm font-semibold text-[#3041a1]">No timeslots in this view</p>
                <p className="mt-1 text-xs text-[#6f7cb6]">
                  Generate a schedule for the selected route to get started.
                </p>
              </div>
            ) : null}

            {!timeslotsQuery.isLoading && !timeslotsQuery.isError && filteredTimeslots.length > 0 ? (
              <div className="space-y-3">
                {filteredTimeslots.map((timeslot) => (
                  <article
                    key={timeslot.id}
                    className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-sm font-semibold text-[#22339a]">
                          {timeslot.routeFrom} - {timeslot.routeTo}
                        </p>
                        <div className="mt-1 grid gap-1 text-[11px] text-[#5564ab]">
                          <p className="inline-flex items-center gap-1.5">
                            <CalendarDays className="h-3.5 w-3.5 text-[#8f9bd7]" />
                            {timeslot.date}
                          </p>
                          <p className="inline-flex items-center gap-1.5">
                            <Clock3 className="h-3.5 w-3.5 text-[#8f9bd7]" />
                            {timeslot.time}
                          </p>
                          <p>
                            Seats: {timeslot.availableSeats}/{timeslot.totalSeats} available, {timeslot.bookedSeats} booked
                          </p>
                        </div>
                      </div>
                      <span
                        className={`rounded-full border px-2 py-1 text-[10px] font-semibold ${timeslotStatusClass(
                          timeslot.status
                        )}`}
                      >
                        {timeslot.status || "ACTIVE"}
                      </span>
                    </div>

                    <div className="mt-3 flex gap-2">
                      <button
                        type="button"
                        onClick={() => selectTimeslot(timeslot)}
                        className="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl border border-[#cbd3f1] bg-white px-3 text-xs font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff]"
                      >
                        <PencilLine className="h-3.5 w-3.5" />
                        Edit
                      </button>
                      {timeslot.status !== "CANCELLED" ? (
                        <button
                          type="button"
                          onClick={() => handleCancelTimeslot(timeslot)}
                          disabled={cancelTimeslot.isPending}
                          className="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-3 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                          Cancel
                        </button>
                      ) : null}
                    </div>
                  </article>
                ))}
              </div>
            ) : null}
          </div>
        </div>

        <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            {selectedTimeslot ? "Edit Timeslot" : "Choose a Timeslot"}
          </p>

          {selectedTimeslot ? (
            <div className="mt-4 space-y-3">
              <div className="rounded-xl bg-[#f7f8fd] p-3 text-sm text-[#4c5ca7]">
                <p><span className="font-semibold text-[#22339a]">Route:</span> {selectedTimeslot.routeFrom} - {selectedTimeslot.routeTo}</p>
                <p><span className="font-semibold text-[#22339a]">Current seats:</span> {selectedTimeslot.totalSeats}</p>
                <p><span className="font-semibold text-[#22339a]">Booked seats:</span> {selectedTimeslot.bookedSeats}</p>
                <p className="mt-2 text-xs text-[#6f7cb6]">
                  Seat count stays fixed here because the current backend does not regenerate seat records on update.
                </p>
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Date</label>
                <input
                  type="date"
                  value={editForm.date}
                  onChange={(event) => setEditForm((current) => ({ ...current, date: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Time</label>
                <input
                  type="time"
                  value={editForm.time}
                  onChange={(event) => setEditForm((current) => ({ ...current, time: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Status</label>
                <select
                  value={editForm.status}
                  onChange={(event) =>
                    setEditForm((current) => ({
                      ...current,
                      status: event.target.value as "ACTIVE" | "CANCELLED",
                    }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              <button
                type="button"
                onClick={handleUpdateTimeslot}
                disabled={isBusy}
                className="h-10 w-full rounded-xl bg-[#3f53c9] text-sm font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
              >
                {updateTimeslot.isPending ? "Saving..." : "Save Timeslot"}
              </button>
            </div>
          ) : (
            <div className="mt-4 rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <CalendarDays className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">Choose a timeslot to inspect</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                The editor on the right lets you adjust date, time, and status only.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
