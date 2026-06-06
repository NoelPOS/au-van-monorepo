package com.auvan.backend.service;

import com.auvan.backend.entity.IdempotencyKey;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyService {

    /**
     * Looks up an existing idempotency key for the given user + scope.
     * Returns an empty Optional if none exists (first-time request).
     */
    Optional<IdempotencyKey> find(UUID userId, String scope, String key);

    /**
     * Creates an IN_PROGRESS record for the given user + scope + key.
     * Computes a SHA-256 hash of the request body for collision detection.
     */
    IdempotencyKey startRequest(UUID userId, String scope, String key, Object requestBody);

    /** Marks the record as COMPLETED and stores the cached response. */
    void completeRequest(UUID recordId, Object responseData, int httpStatus);

    /** Marks the record as FAILED with the given error message. */
    void failRequest(UUID recordId, String errorMessage);

    /** Removes all expired records. Called by a cleanup scheduler. */
    int deleteExpired();
}
