import { apiRequest } from "@/api/client";
import type { PageResponse } from "@/types/api";

export type AuditLogEntry = {
  id: string;
  actorId: string | null;
  action: string;
  targetType: string;
  targetId: string;
  metadata: Record<string, unknown> | null;
  ip: string | null;
  createdAt: string;
};

type AuditLogDto = {
  id: string;
  actorId?: string | null;
  action: string;
  targetType: string;
  targetId: string;
  metadata?: Record<string, unknown> | null;
  ip?: string | null;
  createdAt: string;
};

function mapAuditLog(dto: AuditLogDto): AuditLogEntry {
  return {
    id: dto.id,
    actorId: dto.actorId ?? null,
    action: dto.action,
    targetType: dto.targetType,
    targetId: dto.targetId,
    metadata: dto.metadata ?? null,
    ip: dto.ip ?? null,
    createdAt: dto.createdAt,
  };
}

export function getAuditLogs(page = 0, size = 20) {
  return apiRequest<PageResponse<AuditLogDto>>("/api/admin/audit-logs", {
    query: {
      page,
      size,
    },
  }).then((pageResponse) => ({
    ...pageResponse,
    content: pageResponse.content.map(mapAuditLog),
  }));
}
