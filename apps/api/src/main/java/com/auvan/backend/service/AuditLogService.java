package com.auvan.backend.service;

import com.auvan.backend.dto.response.AuditLogResponse;
import com.auvan.backend.dto.response.PageResponse;

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
