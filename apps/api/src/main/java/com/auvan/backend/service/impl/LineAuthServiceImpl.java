package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.LineAuthRequest;
import com.auvan.backend.dto.response.AuthResponse;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.AuthProvider;
import com.auvan.backend.exception.UnauthorizedException;

import com.auvan.backend.repository.UserRepository;
import com.auvan.backend.security.JwtTokenProvider;
import com.auvan.backend.service.LineAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineAuthServiceImpl implements LineAuthService {

    private static final String LINE_VERIFY_URL = "https://api.line.me/oauth2/v2.1/verify";

    @Value("${line.liff.channel.id}")
    private String liffChannelId;

    private final UserRepository   userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityMappers mappers;
    private final RestClient       restClient;

    @Override
    @Transactional
    public AuthResponse authenticate(LineAuthRequest request) {
        Map<String, Object> lineUser = verifyLineToken(request.idToken());

        String lineUserId  = (String) lineUser.get("sub");
        String displayName = (String) lineUser.get("name");
        String pictureUrl  = (String) lineUser.get("picture");

        if (lineUserId == null) {
            throw new UnauthorizedException("LINE ID token verification failed");
        }

        User user = userRepository.findByLineUserId(lineUserId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setLineUserId(lineUserId);
                    newUser.setAuthProvider(AuthProvider.LINE);
                    newUser.setName(displayName != null ? displayName : "LINE User");
                    return newUser;
                });

        // Always sync latest profile data from LINE
        if (displayName != null) user.setDisplayName(displayName);
        if (pictureUrl  != null) user.setProfileImageUrl(pictureUrl);
        if (request.phone() != null && user.getPhone() == null) user.setPhone(request.phone());

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.isAdmin());
        return new AuthResponse(token, mappers.toUser(user));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyLineToken(String idToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id_token",  idToken);
        form.add("client_id", liffChannelId);

        try {
            return restClient.post()
                    .uri(LINE_VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.error("LINE token verification failed: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid or expired LINE ID token");
        }
    }
}
