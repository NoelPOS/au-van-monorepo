package com.auvan.backend.service;

import java.util.List;
import java.util.Map;

public interface LineWebhookService {

    /**
     * Processes a batch of LINE webhook events.
     *
     * @param events parsed event objects from the LINE webhook payload
     */
    void handle(List<Map<String, Object>> events);
}
