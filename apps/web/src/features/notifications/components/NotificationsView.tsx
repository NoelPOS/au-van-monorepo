import { format, isToday, isYesterday } from "date-fns";
import { Bell, Check, CheckCheck, Inbox, Loader2 } from "lucide-react";
import { useMemo, useState } from "react";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useMarkNotificationRead } from "@/features/notifications/hooks/useMarkNotificationRead";
import { useNotifications } from "@/features/notifications/hooks/useNotifications";
import { useUnreadNotificationCount } from "@/features/notifications/hooks/useUnreadNotificationCount";
import type { NotificationSummary } from "@/types/domain";

type NotificationGroup = {
  label: string;
  items: NotificationSummary[];
};

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function groupNotifications(notifications: NotificationSummary[]): NotificationGroup[] {
  const today: NotificationSummary[] = [];
  const yesterday: NotificationSummary[] = [];
  const earlier: NotificationSummary[] = [];

  notifications.forEach((notification) => {
    const createdAt = new Date(notification.createdAt);

    if (isToday(createdAt)) {
      today.push(notification);
      return;
    }

    if (isYesterday(createdAt)) {
      yesterday.push(notification);
      return;
    }

    earlier.push(notification);
  });

  return [
    { label: "Today", items: today },
    { label: "Yesterday", items: yesterday },
    { label: "Earlier", items: earlier },
  ].filter((group) => group.items.length > 0);
}

export function NotificationsView() {
  const [page, setPage] = useState(0);
  const notificationsQuery = useNotifications({ page, size: 20 });
  const unreadCountQuery = useUnreadNotificationCount();
  const markRead = useMarkNotificationRead();

  const pageData = notificationsQuery.data;
  const notifications = pageData?.content ?? [];
  const groups = useMemo(() => groupNotifications(notifications), [notifications]);

  async function handleMarkRead(notificationId: string) {
    try {
      await markRead.mutateAsync(notificationId);
    } catch {
      // The mutation state already carries the error for the page-level message.
    }
  }

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader
        title="Notifications"
        subtitle="Updates for bookings and payments"
        rightSlot={
          <button
            type="button"
            onClick={() => {
              void Promise.all(
                notifications
                  .filter((notification) => !notification.read)
                  .map((notification) => markRead.mutateAsync(notification.id))
              ).catch(() => {
                // The page-level error message already covers mutation failures.
              });
            }}
            disabled={markRead.isPending || (unreadCountQuery.data ?? 0) === 0}
            className="rounded-md border border-[#ccd4f3] bg-white px-2 py-1 text-[11px] font-medium text-[#3f53c9] disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span className="inline-flex items-center gap-1">
              {markRead.isPending ? <Loader2 className="h-3 w-3 animate-spin" /> : <CheckCheck className="h-3 w-3" />}
              {markRead.isPending ? "Updating..." : "Read all"}
            </span>
          </button>
        }
      />

      {markRead.isError ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          {markRead.error instanceof Error ? markRead.error.message : "Notification update failed"}
        </p>
      ) : null}

      {notificationsQuery.isLoading ? (
        <div className="mt-4 space-y-3">
          <div className="h-20 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
          <div className="h-20 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
        </div>
      ) : null}

      {notificationsQuery.isError ? (
        <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-xs text-amber-700">
          Notifications could not be loaded from the backend.
        </div>
      ) : null}

      {!notificationsQuery.isLoading && !notificationsQuery.isError && notifications.length === 0 ? (
        <div className="mt-4 rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
          <Inbox className="mx-auto h-6 w-6 text-[#98a5da]" />
          <p className="mt-2 text-sm font-semibold text-[#3041a1]">No notifications yet</p>
          <p className="mt-1 text-xs text-[#6f7cb6]">Your booking and payment updates will appear here.</p>
        </div>
      ) : null}

      {!notificationsQuery.isLoading && !notificationsQuery.isError && notifications.length > 0 ? (
        <div className="mt-4 space-y-4">
          {groups.map((group) => (
            <section key={group.label}>
              <h2 className="mb-2 text-[11px] font-semibold uppercase tracking-wide text-[#707db6]">
                {group.label}
              </h2>
              <div className="space-y-2">
                {group.items.map((notification) => (
                  <article
                    key={notification.id}
                    className={`rounded-2xl border px-3 py-3 shadow-[0_8px_20px_rgba(57,85,194,0.05)] ${
                      notification.read
                        ? "border-[#d7dcf4] bg-white"
                        : "border-[#4f62d3] bg-[#5c6fd9] text-white"
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <Bell className={`h-4 w-4 ${notification.read ? "text-[#7784bf]" : "text-white"}`} />
                          <p
                            className={`text-[11px] font-semibold ${
                              notification.read ? "text-[#1f2f8d]" : "text-white"
                            }`}
                          >
                            {notification.title}
                          </p>
                        </div>
                        <p
                          className={`mt-1 text-[10px] leading-relaxed ${
                            notification.read ? "text-[#6470a8]" : "text-white/85"
                          }`}
                        >
                          {notification.message}
                        </p>
                        <div
                          className={`mt-2 flex flex-wrap items-center gap-2 text-[9px] ${
                            notification.read ? "text-[#91a0dd]" : "text-white/70"
                          }`}
                        >
                          <span>{format(new Date(notification.createdAt), "p")}</span>
                          <span className="rounded-full border border-current/20 px-2 py-0.5">
                            {formatLabel(notification.type)}
                          </span>
                        </div>
                      </div>

                      {!notification.read ? (
                        <button
                          type="button"
                          onClick={() => handleMarkRead(notification.id)}
                          disabled={markRead.isPending}
                          className="rounded-lg bg-white/20 p-1.5 text-white transition-colors hover:bg-white/30 disabled:cursor-not-allowed disabled:opacity-70"
                          title="Mark as read"
                        >
                          <Check className="h-3.5 w-3.5" />
                        </button>
                      ) : null}
                    </div>
                  </article>
                ))}
              </div>
            </section>
          ))}

          {(pageData?.totalPages ?? 0) > 1 ? (
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
      ) : null}
    </div>
  );
}
