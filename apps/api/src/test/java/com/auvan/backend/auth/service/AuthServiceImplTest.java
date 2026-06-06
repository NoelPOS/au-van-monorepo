package com.auvan.backend.auth.service;

import com.auvan.backend.auth.dto.LoginRequest;
import com.auvan.backend.auth.dto.RegisterRequest;
import com.auvan.backend.user.dto.UserResponse;
import com.auvan.backend.user.entity.User;
import com.auvan.backend.auth.enums.AuthProvider;
import com.auvan.backend.shared.exception.ConflictException;
import com.auvan.backend.shared.exception.UnauthorizedException;
import com.auvan.backend.shared.mapper.EntityMappers;
import com.auvan.backend.user.repository.UserRepository;
import com.auvan.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EntityMappers mappers;

    @InjectMocks private AuthServiceImpl authService;

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@auvan.app", "secret123", "0812345678");
        UUID userId = UUID.randomUUID();

        User saved = new User();
        saved.setId(userId);
        saved.setName("Alice");
        saved.setEmail("alice@auvan.app");
        saved.setAuthProvider(AuthProvider.LOCAL);
        saved.setCreatedAt(Instant.now());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.isAdmin())).thenReturn("jwt-token");
        when(mappers.toUser(saved)).thenReturn(new UserResponse(
                userId, "alice@auvan.app", null, AuthProvider.LOCAL, null, "Alice", "0812345678",
                null, null, false, Instant.now()
        ));

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().email()).isEqualTo("alice@auvan.app");

        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@auvan.app", "secret123", null);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("alice@auvan.app", "wrong-password");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@auvan.app");
        user.setPasswordHash("stored-hash");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
