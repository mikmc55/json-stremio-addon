package com.stremio.addon.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProviderDto {
    @JsonProperty("provider_id")
    private Integer providerId;
    @JsonProperty("provider_name")
    private String providerName;
    @JsonProperty("logo_path")
    private String logoPath;
}
