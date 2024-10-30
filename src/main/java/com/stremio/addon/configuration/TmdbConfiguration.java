package com.stremio.addon.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "addon.tmbd")
@Data
public class TmdbConfiguration {
    private String apiKey;
    private String apiUrl;
}
