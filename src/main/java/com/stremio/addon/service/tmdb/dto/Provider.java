package com.stremio.addon.service.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Provider {
    @JsonProperty("display_priority")
    private int displayPriority;

    @JsonProperty("logo_path")
    private String logoPath;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("provider_id")
    private int providerId;

    @JsonProperty("display_priorities")
    private Map<String, Integer> displayPriorities;
}
