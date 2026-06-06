import { Link } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { useDashboardStats } from "@/features/dashboard/hooks/useDashboardStats";

type StatCard = {
  label: string;
  value: string;
  tone: string;
  helper: string;
};

type QuickLink = {
  title: string;
  description: string;
  to: string;
  cta: string;
};

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-US").format(value);
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "THB",
    maximumFractionDigits: 2,
  }).format(value);
}

export function AdminDashboardView() {
  const statsQuery = useDashboardStats();
  const stats = statsQuery.data;

  const statCards: StatCard[] = stats
    ? [
        {
          label: "Total Bookings",
          value: formatNumber(stats.totalBookings),
          tone: "border-[#dbe1fb] bg-white text-[#22339a]",
          helper: `${formatNumber(stats.confirmedBookings)} confirmed`,
        },
        {
          label: "Pending Payment",
          value: formatNumber(stats.pendingPaymentBookings),
          tone: "border-amber-200 bg-amber-50 text-amber-700",
          helper: "Needs proof upload or review",
        },
        {
          label: "Cancelled",
          value: formatNumber(stats.cancelledBookings),
          tone: "border-slate-200 bg-slate-100 text-slate-700",
          helper: "Includes expired or cancelled bookings",
        },
        {
          label: "Passengers",
          value: formatNumber(stats.totalPassengers),
          tone: "border-emerald-200 bg-emerald-50 text-emerald-700",
          helper: "Confirmed and completed rides",
        },
        {
          label: "Revenue",
          value: formatCurrency(stats.totalRevenue),
          tone: "border-[#dfe4ff] bg-[#f4f6ff] text-[#3142a5]",
          helper: "Completed payments only",
        },
        {
          label: "Occupancy",
          value: `${stats.averageOccupancyPercent.toFixed(2)}%`,
          tone: "border-cyan-200 bg-cyan-50 text-cyan-700",
          helper: `${formatNumber(stats.totalUsers)} registered users`,
        },
      ]
    : [];

  const quickLinks: QuickLink[] = [
    {
      title: "Bookings",
      description: "Inspect current bookings and handle admin cancellations.",
      to: appRoutes.adminBookings,
      cta: "Open bookings",
    },
    {
      title: "Payments",
      description: "Review submitted payment proof and approve or reject transfers.",
      to: appRoutes.adminPayments,
      cta: "Open payments",
    },
    {
      title: "Routes",
      description: "Maintain route configuration before editing schedules.",
      to: appRoutes.adminRoutes,
      cta: "Open routes",
    },
    {
      title: "Timeslots",
      description: "Generate schedules and adjust future departures safely.",
      to: appRoutes.adminTimeslots,
      cta: "Open timeslots",
    },
    {
      title: "Users",
      description: "Update profile basics and grant or remove admin access.",
      to: appRoutes.adminUsers,
      cta: "Open users",
    },
    {
      title: "Audit Logs",
      description: "Review recent admin activity and system-side changes.",
      to: appRoutes.adminAuditLogs,
      cta: "Open logs",
    },
  ];

  return (
    <section className="space-y-5">
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Dashboard</p>
        <h1 className="mt-1 text-xl font-semibold">Operational snapshot</h1>
        <p className="mt-1 text-sm text-white/80">
          The dashboard now reads real numbers from Spring instead of placeholder migration notes.
        </p>
      </header>

      {statsQuery.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div
              key={index}
              className="h-28 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white"
            />
          ))}
        </div>
      ) : null}

      {statsQuery.isError ? (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          Dashboard stats could not be loaded from the backend.
        </div>
      ) : null}

      {!statsQuery.isLoading && !statsQuery.isError && stats ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {statCards.map((card) => (
            <article
              key={card.label}
              className={`rounded-2xl border p-5 shadow-[0_8px_20px_rgba(57,85,194,0.06)] ${card.tone}`}
            >
              <p className="text-[11px] font-semibold uppercase tracking-[0.18em] opacity-75">{card.label}</p>
              <h2 className="mt-3 text-3xl font-semibold">{card.value}</h2>
              <p className="mt-2 text-sm opacity-80">{card.helper}</p>
            </article>
          ))}
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {quickLinks.map((item) => (
          <article
            key={item.title}
            className="rounded-2xl border border-[#d8def5] bg-white p-5 shadow-[0_8px_20px_rgba(57,85,194,0.06)]"
          >
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#7c89c0]">{item.title}</p>
            <p className="mt-3 text-sm text-[#6674b0]">{item.description}</p>
            <Link
              to={item.to}
              className="mt-4 inline-flex rounded-full bg-[#445bd0] px-4 py-2 text-sm font-semibold text-white"
            >
              {item.cta}
            </Link>
          </article>
        ))}
      </section>
    </section>
  );
}
