package com.auvan.backend.service;

import com.auvan.backend.dto.request.UpdateUserRequest;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.dto.response.UserResponse;
import com.auvan.backend.entity.User;

import java.util.UUID;

public interface UserService {

    UserResponse getById(UUID id);

    User getEntityById(UUID id);

    UserResponse updateProfile(UUID userId, UpdateUserRequest request);

    PageResponse<UserResponse> listUsers(int page, int size);

    UserResponse adminUpdateUser(UUID targetId, UpdateUserRequest request);
}
