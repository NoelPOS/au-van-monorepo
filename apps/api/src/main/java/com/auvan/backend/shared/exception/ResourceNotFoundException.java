package com.auvan.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AuVanException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public static ResourceNotFoundException of(String entity, Object id) {
        return new ResourceNotFoundException(entity + " not found: " + id);
    }
}
