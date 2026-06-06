import { useState } from "react";
import { useAuditLogs } from "@/features/audit-logs/hooks/useAuditLogs";

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatTime(value: string) {
  return new Date(value).toLocaleString("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function AdminAuditLogsView() {
  const [page, setPage] = useState(0);
  const auditLogsQuery = useAuditLogs(page, 20);
  const pageData = auditLogsQuery.data;
  const logs = pageData?.content ?? [];

  return (
    <section className="space-y-5">
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Audit Logs</p>
        <h1 className="mt-1 text-xl font-semibold">Recent admin and system activity</h1>
        <p className="mt-1 text-sm text-white/80">
          This keeps operations transparent without inventing a more complicated reporting module.
        </p>
      </header>

      {auditLogsQuery.isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <div
              key={index}
              className="h-32 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white"
            />
          ))}
        </div>
      ) : null}

      {auditLogsQuery.isError ? (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          Audit logs could not be loaded from the backend.
        </div>
      ) : null}

      {!auditLogsQuery.isLoading && !auditLogsQuery.isError && logs.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
          <p className="text-sm font-semibold text-[#3041a1]">No audit logs yet</p>
          <p className="mt-1 text-xs text-[#6f7cb6]">
            Once admin actions and system events are recorded, they will appear here.
          </p>
        </div>
      ) : null}

      {!auditLogsQuery.isLoading && !auditLogsQuery.isError && logs.length > 0 ? (
        <div className="space-y-3">
          {logs.map((log) => (
            <article
              key={log.id}
              className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-[#22339a]">{formatLabel(log.action)}</p>
                  <p className="mt-1 text-xs text-[#5564ab]">
                    {formatLabel(log.targetType)}: {log.targetId || "-"}
                  </p>
                </div>
                <p className="text-xs text-[#6f7cb6]">{formatTime(log.createdAt)}</p>
              </div>

              <div className="mt-3 grid gap-1 text-[11px] text-[#5564ab] md:grid-cols-2">
                <p>Actor ID: {log.actorId || "-"}</p>
                <p>IP: {log.ip || "-"}</p>
              </div>

              {log.metadata && Object.keys(log.metadata).length > 0 ? (
                <pre className="mt-3 overflow-x-auto rounded-xl bg-[#f7f8fd] p-3 text-[11px] text-[#4c5ca7]">
                  {JSON.stringify(log.metadata, null, 2)}
                </pre>
              ) : null}
            </article>
          ))}
        </div>
      ) : null}

      {!auditLogsQuery.isLoading && !auditLogsQuery.isError && (pageData?.totalPages ?? 0) > 1 ? (
        <div className="flex items-center justify-between rounded-2xl border border-[#d6dcf4] bg-white px-4 py-3">
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
              setPage((current) => Math.min((pageData?.totalPages ?? 1) - 1, current + 1))
            }
            disabled={page >= (pageData?.totalPages ?? 1) - 1}
            className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
          >
            Next
          </button>
        </div>
      ) : null}
    </section>
  );
}
