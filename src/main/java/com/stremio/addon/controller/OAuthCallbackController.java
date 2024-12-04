package com.stremio.addon.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuthCallbackController {

    @Value("${trakt.client-id}")
    private String clientId;

    @Value("${trakt.client-secret}")
    private String clientSecret;

    @Value("${trakt.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://api.trakt.tv/oauth/token";

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Construir la solicitud para obtener el token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("redirect_uri", redirectUri);
        body.put("grant_type", "authorization_code");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Realizar la solicitud para obtener el token
        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            return ResponseEntity.ok("Access Token: " + responseBody.get("access_token"));
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Error retrieving token");
        }
    }
}

