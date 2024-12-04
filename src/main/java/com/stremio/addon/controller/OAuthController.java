package com.stremio.addon.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class OAuthController {

    @Value("${trakt.client-id}")
    private String clientId;

    @Value("${trakt.redirect-uri}")
    private String redirectUri;

    private static final String AUTH_URL = "https://trakt.tv/oauth/authorize";

    @GetMapping("/oauth/login")
    public RedirectView login() {
        String url = AUTH_URL + "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri;
        return new RedirectView(url);
    }
}
