package com.auvan.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void cleanDatabase() {
        resetDatabase();
    }

    @Test
    @SuppressWarnings("unchecked")
    void registerLoginAndAccessProtectedRoute() {
        Map<String, Object> registerRequest = Map.of(
                "name", "Integration User",
                "email", "integration.user@auvan.app",
                "password", "secret123",
                "phone", "0812345678"
        );

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/auth/register",
                jsonRequest(registerRequest),
                Map.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().get("success")).isEqualTo(true);

        Map<String, Object> loginRequest = Map.of(
                "email", "integration.user@auvan.app",
                "password", "secret123"
        );

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                jsonRequest(loginRequest),
                Map.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> loginBody = loginResponse.getBody();
        assertThat(loginBody).isNotNull();
        Map<String, Object> loginData = (Map<String, Object>) loginBody.get("data");
        String token = (String) loginData.get("token");
        assertThat(token).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> routesResponse = restTemplate.exchange(
                "/api/liff/routes",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(routesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(routesResponse.getBody()).isNotNull();
        assertThat(routesResponse.getBody().get("success")).isEqualTo(true);
    }

    private HttpEntity<Map<String, Object>> jsonRequest(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }
}
