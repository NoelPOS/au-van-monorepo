package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.response.AuditLogResponse;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.entity.AuditLog;

import com.auvan.backend.repository.AuditLogRepository;
import com.auvan.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EntityMappers mappers;

    /**
     * Fire-and-forget: runs asynchronously in a separate transaction so a failure
     * here never rolls back the caller's business transaction.
     */
    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID actorId,
                    String action,
                    String targetType,
                    String targetId,
                    Map<String, Object> metadata,
                    String ip,
                    String userAgent) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorId(actorId);
            entry.setAction(action);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId);
            entry.setMetadata(metadata);
            entry.setIp(ip);
            entry.setUserAgent(userAgent);
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("Failed to persist audit log [action={}]: {}", action, ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(mappers::toAuditLog));
    }
}
