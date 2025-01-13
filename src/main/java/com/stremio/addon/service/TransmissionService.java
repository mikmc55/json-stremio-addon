package com.stremio.addon.service;

import com.stremio.addon.repository.SearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransmissionService {

    @Value("${transmission.url:http://localhost:9091/transmission/rpc}")
    private String transmissionUrl;

    @Value("${transmission.username:transmission}")
    private String username;

    @Value("${transmission.password:transmission}")
    private String password;

    private final RestTemplate restTemplate;
    private final SearchRepository searchRepository;
    private String sessionId;

    public TransmissionService(@Qualifier("restTemplateJson") RestTemplate restTemplate, SearchRepository searchRepository) {
        this.restTemplate = restTemplate;
        this.searchRepository = searchRepository;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        if (sessionId != null) {
            headers.set("X-Transmission-Session-Id", sessionId);
        }
        return headers;
    }

    public String generateMagnetLink(String torrentFilePath) throws IOException {
        log.info("Generating magnet link for file: {}", torrentFilePath);
        File torrentFile = validateAndReadFile(torrentFilePath);
        byte[] torrentData = Files.readAllBytes(torrentFile.toPath());
        return generateMagnetLink(torrentData);
    }

    public String generateMagnetLink(byte[] torrentData) {
        String infoHash = computeInfoHash(torrentData);
        String magnetLink = "magnet:?xt=urn:btih:" + infoHash;
        log.info("Magnet link generated: {}", magnetLink);
        return magnetLink;
    }

    private String computeInfoHash(byte[] torrentData) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(torrentData);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error computing info hash: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to compute info hash.", e);
        }
    }

    public void uploadTorrent(String torrentBase64) {
        String payload = String.format("{\"method\":\"torrent-add\",\"arguments\":{\"metainfo\":\"%s\"}}", torrentBase64);
        executeRequest(payload, "Torrent uploaded successfully.", "Failed to upload torrent");
    }

    public void addTorrentFile(String torrentFilePath) throws IOException {
        log.info("Attempting to add torrent file: {}", torrentFilePath);
        File file = validateAndReadFile(torrentFilePath);
        String base64 = encodeFileToBase64(file);
        uploadTorrent(base64);
    }

    public void addTorrentFromSearchId(Integer searchId) {
        log.info("Attempting to add torrent from search ID: {}", searchId);
        searchRepository.findById(searchId).ifPresent(search -> {
            search.getTorrents().forEach(torrent -> {
                uploadTorrent(Base64.getEncoder().encodeToString(torrent.getContent()));
            });
        });
    }

    private File validateAndReadFile(String torrentFilePath) {
        File torrentFile = new File(torrentFilePath);
        if (!torrentFile.exists()) {
            throw new IllegalArgumentException("Torrent file does not exist: " + torrentFilePath);
        }
        log.debug("Validated torrent file: {}", torrentFilePath);
        return torrentFile;
    }

    private String encodeFileToBase64(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }

    public void configureWebhook(String webhookScript) {
        String payload = String.format("{\"method\":\"session-set\",\"arguments\":{\"script-torrent-done-filename\":\"%s\"}}", webhookScript);
        executeRequest(payload, "Webhook configured successfully.", "Failed to configure webhook");
    }

    public List<Map<String, Object>> checkActiveDownloads() {
        String payload = "{\"method\":\"torrent-get\",\"arguments\":{\"fields\":[\"id\",\"name\",\"percentDone\",\"status\"]}}";
        try {
            return handleRequestWithSession(payload);
        } catch (Exception e) {
            log.error("Error checking active downloads: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void executeRequest(String payload, String successMessage, String failureMessage) {
        try {
            handleRequestWithSession(payload);
            log.info(successMessage);
        } catch (Exception e) {
            log.error("{}: {}", failureMessage, e.getMessage(), e);
            throw new RuntimeException(failureMessage, e);
        }
    }

    private List<Map<String, Object>> handleRequestWithSession(String payload) {
        try {
            HttpEntity<String> request = new HttpEntity<>(payload, createAuthHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(transmissionUrl, HttpMethod.POST, request, Map.class);
            return (List<Map<String, Object>>) ((Map) response.getBody().get("arguments")).get("torrents");
        } catch (HttpClientErrorException.Conflict e) {
            String newSessionId = e.getResponseHeaders().getFirst("X-Transmission-Session-Id");
            if (newSessionId != null) {
                sessionId = newSessionId;
                log.info("Updated session ID: {}", sessionId);
                return handleRequestWithSession(payload); // Retry with updated session ID
            }
            throw e;
        }
    }

    public void removeTorrent(int torrentId, boolean deleteData) {
        String payload = String.format("{\"method\":\"torrent-remove\",\"arguments\":{\"ids\":[%d],\"delete-local-data\":%b}}", torrentId, deleteData);
        try {
            handleRequestWithSession(payload);
            log.info("Torrent with ID {} removed successfully.", torrentId);
        } catch (Exception e) {
            log.error("Failed to remove torrent with ID {}: {}", torrentId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove torrent", e);
        }
    }

}
