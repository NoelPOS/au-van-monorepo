package com.auvan.backend.line.controller;

import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.shared.exception.UnauthorizedException;
import com.auvan.backend.line.service.LineWebhookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/line")
public class LineWebhookController {

    @Value("${line.channel.secret:}")
    private String lineChannelSecret;

    private final LineWebhookService lineWebhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Line-Signature", required = false) String signatureHeader) {
        if (!isValidSignature(rawBody, signatureHeader)) {
            throw new UnauthorizedException("Invalid LINE webhook signature");
        }

        List<Map<String, Object>> events = extractEvents(rawBody);
        lineWebhookService.handle(events);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed"));
    }

    private List<Map<String, Object>> extractEvents(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode eventsNode = root.path("events");

            List<Map<String, Object>> events = new ArrayList<>();
            if (eventsNode.isArray()) {
                for (JsonNode eventNode : eventsNode) {
                    Map<String, Object> event = objectMapper.convertValue(
                            eventNode, new TypeReference<Map<String, Object>>() {});
                    events.add(event);
                }
            }

            return events;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid LINE webhook payload");
        }
    }

    private boolean isValidSignature(String rawBody, String providedSignature) {
        if (!StringUtils.hasText(lineChannelSecret) || !StringUtils.hasText(providedSignature)) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(lineChannelSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getEncoder().encodeToString(digest);

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    providedSignature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            return false;
        }
    }
}
