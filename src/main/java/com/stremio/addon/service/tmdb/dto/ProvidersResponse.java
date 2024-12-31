package com.stremio.addon.service.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProvidersResponse {
    @JsonProperty("results")
    private List<Provider> results;
}
