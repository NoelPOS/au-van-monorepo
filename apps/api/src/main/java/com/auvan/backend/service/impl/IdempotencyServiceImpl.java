package com.auvan.backend.service.impl;

import com.auvan.backend.entity.IdempotencyKey;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.IdempotencyStatus;
import com.auvan.backend.exception.ResourceNotFoundException;
import com.auvan.backend.repository.IdempotencyKeyRepository;
import com.auvan.backend.repository.UserRepository;
import com.auvan.backend.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserRepository           userRepository;
    private final ObjectMapper             objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> find(UUID userId, String scope, String key) {
        return idempotencyKeyRepository.findByUserIdAndScopeAndKey(userId, scope, key);
    }

    @Override
    @Transactional
    public IdempotencyKey startRequest(UUID userId, String scope, String key, Object requestBody) {
        User user = userRepository.getReferenceById(userId);

        IdempotencyKey record = new IdempotencyKey();
        record.setUser(user);
        record.setScope(scope);
        record.setKey(key);
        record.setRequestHash(sha256(requestBody));
        record.setStatus(IdempotencyStatus.IN_PROGRESS);
        record.setExpiresAt(Instant.now().plusSeconds(86_400)); // 24 hours

        return idempotencyKeyRepository.save(record);
    }

    @Override
    @Transactional
    public void completeRequest(UUID recordId, Object responseData, int httpStatus) {
        IdempotencyKey record = findOrThrow(recordId);
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setResponseData(responseData);
        record.setResponseStatus(httpStatus);
        idempotencyKeyRepository.save(record);
    }

    @Override
    @Transactional
    public void failRequest(UUID recordId, String errorMessage) {
        IdempotencyKey record = findOrThrow(recordId);
        record.setStatus(IdempotencyStatus.FAILED);
        record.setErrorMessage(errorMessage);
        idempotencyKeyRepository.save(record);
    }

    @Override
    @Transactional
    public int deleteExpired() {
        return idempotencyKeyRepository.deleteExpired(Instant.now());
    }

    private IdempotencyKey findOrThrow(UUID id) {
        return idempotencyKeyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("IdempotencyKey", id));
    }

    private String sha256(Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            log.warn("SHA-256 hash failed, using fallback: {}", ex.getMessage());
            return String.valueOf(body.hashCode());
        }
    }
}
