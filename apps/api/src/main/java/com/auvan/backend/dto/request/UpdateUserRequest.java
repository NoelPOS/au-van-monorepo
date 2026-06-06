package com.auvan.backend.dto.request;

public record UpdateUserRequest(
        String  name,
        String  phone,
        String  defaultPickupLocation,
        String  profileImageUrl,
        Boolean isAdmin
) {}
