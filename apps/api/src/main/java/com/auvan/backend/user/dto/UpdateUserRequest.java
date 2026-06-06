package com.auvan.backend.user.dto;

public record UpdateUserRequest(
        String  name,
        String  phone,
        String  defaultPickupLocation,
        String  profileImageUrl,
        Boolean isAdmin
) {}
