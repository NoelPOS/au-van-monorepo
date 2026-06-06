package com.auvan.backend.audit.service;

import com.auvan.backend.audit.dto.AuditLogResponse;
import com.auvan.backend.shared.dto.PageResponse;
import java.util.Map;
import java.util.UUID;

public interface AuditLogService {

    void log(UUID actorId,
             String action,
             String targetType,
             String targetId,
             Map<String, Object> metadata,
             String ip,
             String userAgent);

    PageResponse<AuditLogResponse> listAll(int page, int size);
}
