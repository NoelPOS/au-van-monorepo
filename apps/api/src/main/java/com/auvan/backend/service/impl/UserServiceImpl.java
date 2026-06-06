package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.UpdateUserRequest;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.dto.response.UserResponse;
import com.auvan.backend.entity.User;
import com.auvan.backend.exception.ResourceNotFoundException;

import com.auvan.backend.repository.UserRepository;
import com.auvan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return mappers.toUser(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateUserRequest request) {
        User user = getEntityById(userId);
        applyUpdates(user, request, false);
        return mappers.toUser(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(userRepository.findAll(pageable).map(mappers::toUser));
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUser(UUID targetId, UpdateUserRequest request) {
        User user = getEntityById(targetId);
        applyUpdates(user, request, true);
        return mappers.toUser(userRepository.save(user));
    }

    private void applyUpdates(User user, UpdateUserRequest request, boolean allowAdminToggle) {
        if (StringUtils.hasText(request.name()))               user.setName(request.name());
        if (StringUtils.hasText(request.phone()))              user.setPhone(request.phone());
        if (request.defaultPickupLocation() != null)           user.setDefaultPickupLocation(request.defaultPickupLocation());
        if (StringUtils.hasText(request.profileImageUrl()))    user.setProfileImageUrl(request.profileImageUrl());
        if (allowAdminToggle && request.isAdmin() != null)     user.setAdmin(request.isAdmin());
    }
}
