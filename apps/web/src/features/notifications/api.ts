import { apiRequest } from "@/api/client";
import type { PageResponse } from "@/types/api";
import type { NotificationSummary } from "@/types/domain";

export type NotificationsQuery = {
  page?: number;
  size?: number;
};

type NotificationDto = {
  id: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  channel: string;
  deliveryStatus: string;
  data?: Record<string, unknown> | null;
  createdAt: string;
};

function mapNotification(dto: NotificationDto): NotificationSummary {
  return {
    id: dto.id,
    type: dto.type,
    title: dto.title,
    message: dto.message,
    read: dto.read,
    channel: dto.channel,
    deliveryStatus: dto.deliveryStatus,
    data: dto.data,
    createdAt: dto.createdAt,
  };
}

export function getNotifications(query: NotificationsQuery = {}) {
  return apiRequest<PageResponse<NotificationDto>>("/api/liff/notifications", {
    query: {
      page: query.page ?? 0,
      size: query.size ?? 20,
    },
  }).then((page) => ({
    ...page,
    content: page.content.map(mapNotification),
  }));
}

export function getUnreadNotificationCount() {
  return apiRequest<{ unread: number }>("/api/liff/notifications/unread-count").then(
    (payload) => payload.unread
  );
}

export function markNotificationRead(notificationId: string) {
  return apiRequest<NotificationDto>(`/api/liff/notifications/${notificationId}/read`, {
    method: "PUT",
  }).then(mapNotification);
}
