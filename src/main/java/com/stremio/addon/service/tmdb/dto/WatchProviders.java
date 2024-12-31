package com.stremio.addon.service.tmdb.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class WatchProviders {
    private Map<String, RegionData> results;

    @Data
    @NoArgsConstructor
    public static class RegionData {
        private String link;
        private List<Provider> buy;
        private List<Provider> rent;
        private List<Provider> flatrate;
    }
}
