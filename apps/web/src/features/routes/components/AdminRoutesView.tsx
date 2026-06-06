import { MapPinned, PencilLine, Plus, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { useAdminRoutes } from "@/features/routes/hooks/useAdminRoutes";
import { useCreateRoute } from "@/features/routes/hooks/useCreateRoute";
import { useDeactivateRoute } from "@/features/routes/hooks/useDeactivateRoute";
import { useUpdateRoute } from "@/features/routes/hooks/useUpdateRoute";
import type { RouteSummary } from "@/types/domain";

type RouteForm = {
  fromLocation: string;
  toLocation: string;
  price: string;
  durationMinutes: string;
  status: "ACTIVE" | "INACTIVE";
};

const emptyRouteForm: RouteForm = {
  fromLocation: "",
  toLocation: "",
  price: "",
  durationMinutes: "",
  status: "ACTIVE",
};

function toRouteForm(route: RouteSummary): RouteForm {
  return {
    fromLocation: route.from,
    toLocation: route.to,
    price: String(route.price),
    durationMinutes: route.durationMinutes ? String(route.durationMinutes) : "",
    status: route.status === "INACTIVE" ? "INACTIVE" : "ACTIVE",
  };
}

function routeStatusClass(status?: string) {
  if (status === "INACTIVE") {
    return "border-slate-200 bg-slate-100 text-slate-600";
  }

  return "border-emerald-200 bg-emerald-50 text-emerald-700";
}

export function AdminRoutesView() {
  const routesQuery = useAdminRoutes();
  const createRoute = useCreateRoute();
  const updateRoute = useUpdateRoute();
  const deactivateRoute = useDeactivateRoute();

  const [selectedRoute, setSelectedRoute] = useState<RouteSummary | null>(null);
  const [form, setForm] = useState<RouteForm>(emptyRouteForm);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const routes = routesQuery.data ?? [];
  const sortedRoutes = useMemo(
    () => [...routes].sort((left, right) => `${left.from}-${left.to}`.localeCompare(`${right.from}-${right.to}`)),
    [routes]
  );

  function startCreateMode() {
    setSelectedRoute(null);
    setForm(emptyRouteForm);
    setMessage(null);
  }

  function startEditMode(route: RouteSummary) {
    setSelectedRoute(route);
    setForm(toRouteForm(route));
    setMessage(null);
  }

  async function handleSubmit() {
    setMessage(null);

    try {
      if (selectedRoute) {
        await updateRoute.mutateAsync({
          routeId: selectedRoute.id,
          fromLocation: form.fromLocation,
          toLocation: form.toLocation,
          price: Number(form.price),
          durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : undefined,
          status: form.status,
        });

        setMessage({ type: "success", text: "Route updated." });
      } else {
        await createRoute.mutateAsync({
          fromLocation: form.fromLocation,
          toLocation: form.toLocation,
          price: Number(form.price),
          durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : undefined,
        });

        setMessage({ type: "success", text: "Route created." });
        setForm(emptyRouteForm);
      }
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Route save failed",
      });
    }
  }

  async function handleDeactivate(route: RouteSummary) {
    setMessage(null);

    try {
      await deactivateRoute.mutateAsync(route.id);
      if (selectedRoute?.id === route.id) {
        setSelectedRoute(null);
        setForm(emptyRouteForm);
      }
      setMessage({ type: "success", text: "Route marked inactive." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Route deactivation failed",
      });
    }
  }

  const isSaving =
    createRoute.isPending || updateRoute.isPending || deactivateRoute.isPending;

  return (
    <section>
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Routes</p>
        <h1 className="mt-1 text-xl font-semibold">Create and maintain travel routes</h1>
        <p className="mt-1 text-sm text-white/80">
          Keep route configuration simple here first, then timeslots can attach to the routes cleanly.
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

      <div className="mt-5 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <div>
          <div className="mb-4 flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-[#22339a]">Routes</p>
              <p className="text-xs text-[#6f7cb6]">{sortedRoutes.length} configured</p>
            </div>
            <button
              type="button"
              onClick={startCreateMode}
              className="inline-flex h-10 items-center justify-center gap-1.5 rounded-xl bg-[#3f53c9] px-4 text-sm font-semibold text-white transition-colors hover:bg-[#3447b4]"
            >
              <Plus className="h-4 w-4" />
              New Route
            </button>
          </div>

          {routesQuery.isLoading ? (
            <div className="space-y-3">
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
            </div>
          ) : null}

          {routesQuery.isError ? (
            <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-700">
              Admin routes could not be loaded from the backend.
            </div>
          ) : null}

          {!routesQuery.isLoading && !routesQuery.isError && sortedRoutes.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <MapPinned className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">No routes yet</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">Create the first route to start defining trip options.</p>
            </div>
          ) : null}

          {!routesQuery.isLoading && !routesQuery.isError && sortedRoutes.length > 0 ? (
            <div className="space-y-3">
              {sortedRoutes.map((route) => (
                <article
                  key={route.id}
                  className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-sm font-semibold text-[#22339a]">
                        {route.from} - {route.to}
                      </p>
                      <div className="mt-1 flex flex-wrap gap-2 text-[11px] text-[#5564ab]">
                        <span>{route.price} THB</span>
                        <span>{route.durationMinutes ? `${route.durationMinutes} min` : "Duration optional"}</span>
                      </div>
                    </div>
                    <span
                      className={`rounded-full border px-2 py-1 text-[10px] font-semibold ${routeStatusClass(
                        route.status
                      )}`}
                    >
                      {route.status || "ACTIVE"}
                    </span>
                  </div>

                  <div className="mt-3 flex gap-2">
                    <button
                      type="button"
                      onClick={() => startEditMode(route)}
                      className="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl border border-[#cbd3f1] bg-white px-3 text-xs font-semibold text-[#3142a5] transition-colors hover:bg-[#f2f5ff]"
                    >
                      <PencilLine className="h-3.5 w-3.5" />
                      Edit
                    </button>
                    {route.status !== "INACTIVE" ? (
                      <button
                        type="button"
                        onClick={() => handleDeactivate(route)}
                        disabled={deactivateRoute.isPending}
                        className="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-3 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                        Mark Inactive
                      </button>
                    ) : null}
                  </div>
                </article>
              ))}
            </div>
          ) : null}
        </div>

        <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            {selectedRoute ? "Edit Route" : "Create Route"}
          </p>

          <div className="mt-4 space-y-3">
            <div>
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                From Location
              </label>
              <input
                value={form.fromLocation}
                onChange={(event) =>
                  setForm((current) => ({ ...current, fromLocation: event.target.value }))
                }
                className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                placeholder="Assumption University"
              />
            </div>

            <div>
              <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                To Location
              </label>
              <input
                value={form.toLocation}
                onChange={(event) =>
                  setForm((current) => ({ ...current, toLocation: event.target.value }))
                }
                className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                placeholder="Mega Bangna"
              />
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Price (THB)
                </label>
                <input
                  type="number"
                  min="1"
                  value={form.price}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, price: event.target.value }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Duration (min)
                </label>
                <input
                  type="number"
                  min="0"
                  value={form.durationMinutes}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, durationMinutes: event.target.value }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                  placeholder="Optional"
                />
              </div>
            </div>

            {selectedRoute ? (
              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Status
                </label>
                <select
                  value={form.status}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      status: event.target.value as "ACTIVE" | "INACTIVE",
                    }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>
            ) : null}

            <button
              type="button"
              onClick={handleSubmit}
              disabled={
                isSaving ||
                !form.fromLocation.trim() ||
                !form.toLocation.trim() ||
                !form.price ||
                Number(form.price) <= 0
              }
              className="h-10 w-full rounded-xl bg-[#3f53c9] text-sm font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
            >
              {selectedRoute
                ? updateRoute.isPending
                  ? "Saving..."
                  : "Save Route"
                : createRoute.isPending
                  ? "Creating..."
                  : "Create Route"}
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
