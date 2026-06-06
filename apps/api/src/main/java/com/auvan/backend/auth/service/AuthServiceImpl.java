package com.auvan.backend.auth.service;

import com.auvan.backend.shared.mapper.EntityMappers;
import com.auvan.backend.auth.dto.LoginRequest;
import com.auvan.backend.auth.dto.RegisterRequest;
import com.auvan.backend.auth.dto.AuthResponse;
import com.auvan.backend.user.entity.User;
import com.auvan.backend.auth.enums.AuthProvider;
import com.auvan.backend.shared.exception.ConflictException;
import com.auvan.backend.shared.exception.UnauthorizedException;
import com.auvan.backend.user.repository.UserRepository;
import com.auvan.backend.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityMappers mappers;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists", "EMAIL_TAKEN");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setAuthProvider(AuthProvider.LOCAL);

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.isAdmin());
        return new AuthResponse(token, mappers.toUser(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.isAdmin());
        return new AuthResponse(token, mappers.toUser(user));
    }
}
