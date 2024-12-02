package com.stremio.addon.controller;

import com.stremio.addon.controller.dto.CatalogContainer;
import com.stremio.addon.controller.dto.Manifest;
import com.stremio.addon.controller.dto.AddonSearchResult;
import com.stremio.addon.service.AddonSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(produces = "application/json") // Indica que todos los endpoints deben devolver JSON
public class AddonController {

    @Autowired
    private AddonSearchService addonSearchService;

    // Ruta para las series con temporada y episodio
    @GetMapping(value = "/stream/series/{id}:{season}:{episode}.json", produces = "application/json")
    @CrossOrigin
    public AddonSearchResult seriesStreamHandler(@PathVariable String id,
                                                 @PathVariable(required = false) String season,
                                                 @PathVariable(required = false) String episode) {
        var streams = addonSearchService.searchTorrent("series", id, season, episode);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping(value = "/stream/movie/{id}.json", produces = "application/json")
    @CrossOrigin
    public AddonSearchResult movieStreamHandler(@PathVariable String id) {
        var streams = addonSearchService.searchTorrent("movie", id);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping(value = "/manifest.json", produces = "application/json")
    @CrossOrigin
    public Manifest getManifest() {
        return addonSearchService.getManifest();
    }

    @RequestMapping(value = {"/catalog/{type}/{id}.json", "/catalog/{type}/{id}/{extra}.json"}, produces = "application/json")
    @CrossOrigin
    public CatalogContainer getCatalog(@PathVariable("type") String type, @PathVariable("id") String id, @PathVariable("extra") Optional<String> extra) {
        return addonSearchService.getCatalog(type, id, extra);
    }
}
