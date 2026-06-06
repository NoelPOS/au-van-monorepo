package com.auvan.backend.line.service;

import com.auvan.backend.line.service.LineWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineWebhookServiceImpl implements LineWebhookService {

    @Override
    public void handle(List<Map<String, Object>> events) {
        for (Map<String, Object> event : events) {
            String type = (String) event.get("type");
            log.info("Received LINE webhook event: type={}", type);

            try {
                switch (type != null ? type : "") {
                    case "follow"   -> handleFollow(event);
                    case "unfollow" -> handleUnfollow(event);
                    case "message"  -> handleMessage(event);
                    default         -> log.debug("Unhandled LINE event type: {}", type);
                }
            } catch (Exception ex) {
                log.error("Error handling LINE event [type={}]: {}", type, ex.getMessage());
            }
        }
    }

    private void handleFollow(Map<String, Object> event) {
        log.info("LINE user followed: {}", extractUserId(event));
    }

    private void handleUnfollow(Map<String, Object> event) {
        log.info("LINE user unfollowed: {}", extractUserId(event));
    }

    private void handleMessage(Map<String, Object> event) {
        log.info("LINE message from user: {}", extractUserId(event));
        // Future: parse message, respond via LINE Messaging API
    }

    @SuppressWarnings("unchecked")
    private String extractUserId(Map<String, Object> event) {
        Map<String, Object> source = (Map<String, Object>) event.get("source");
        return source != null ? (String) source.get("userId") : "unknown";
    }
}
