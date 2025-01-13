package com.stremio.addon.controller;

import com.stremio.addon.service.WebhookProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookProcessor webhookProcessor;

    @PostMapping("/download-complete")
    public ResponseEntity<String> handleDownloadComplete(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received download complete webhook: {}", payload);
            webhookProcessor.processDownloadComplete(payload);
            return ResponseEntity.ok("Download processed successfully");
        } catch (Exception e) {
            log.error("Error processing download complete webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error processing download complete");
        }
    }
}
