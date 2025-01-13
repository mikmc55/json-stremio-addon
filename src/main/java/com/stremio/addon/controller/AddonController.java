
package com.stremio.addon.controller;

import com.stremio.addon.controller.dto.AddonSearchResult;
import com.stremio.addon.controller.dto.Manifest;
import com.stremio.addon.service.AddonSearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json") // Indica que todos los endpoints deben devolver JSON
@Validated
@RequiredArgsConstructor
public class AddonController {

    private final AddonSearchService addonSearchService;

    @GetMapping(value = "/stream/series/{id}:{season}:{episode}.json", produces = "application/json")
    @CrossOrigin
    public AddonSearchResult seriesStreamHandler(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String id,
            @PathVariable(required = false) String season,
            @PathVariable(required = false) String episode) {
        log.info("Fetching streams for series: id={}, season={}, episode={}", id, season, episode);
        var streams = addonSearchService.searchSeriesTorrent(id, season, episode);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping(value = "/stream/movie/{id}.json", produces = "application/json")
    @CrossOrigin
    public AddonSearchResult movieStreamHandler(
            @PathVariable @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String id) {
        log.info("Fetching streams for movie: id={}", id);
        var streams = addonSearchService.searchMoviesTorrent(id);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping(value = "/manifest.json", produces = "application/json")
    @CrossOrigin
    public Manifest getManifest() {
        log.info("Fetching addon manifest");
        return addonSearchService.getManifest();
    }
}