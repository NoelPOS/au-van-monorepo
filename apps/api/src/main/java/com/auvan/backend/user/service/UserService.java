package com.auvan.backend.user.service;

import com.auvan.backend.user.dto.UpdateUserRequest;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.user.dto.UserResponse;
import com.auvan.backend.user.User;

import java.util.UUID;

public interface UserService {

    UserResponse getById(UUID id);

    User getEntityById(UUID id);

    UserResponse updateProfile(UUID userId, UpdateUserRequest request);

    PageResponse<UserResponse> listUsers(int page, int size);

    UserResponse adminUpdateUser(UUID targetId, UpdateUserRequest request);
}
