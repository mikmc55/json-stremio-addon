package com.stremio.addon.controller;

import com.stremio.addon.controller.dto.CatalogContainer;
import com.stremio.addon.controller.dto.Manifest;
import com.stremio.addon.controller.dto.AddonSearchResult;
import com.stremio.addon.service.AddonSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AddonController {
    @Autowired
    private AddonSearchService addonSearchService;


    // Ruta para las series con temporada y episodio
    @GetMapping("/stream/series/{id}:{season}:{episode}.json")
    @CrossOrigin
    public AddonSearchResult seriesStreamHandler(@PathVariable String id,
                                                 @PathVariable(required = false) String season,
                                                 @PathVariable(required = false) String episode) {
        // Usar la estrategia de búsqueda de series con temporada y episodio
        var streams = addonSearchService.searchTorrent("series", id, season, episode);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping("/stream/movie/{id}.json")
    @CrossOrigin
    public AddonSearchResult movieStreamHandler(@PathVariable String id) {
        // Usar la estrategia de búsqueda de series con temporada y episodio
        var streams = addonSearchService.searchTorrent("movie", id);
        return AddonSearchResult.builder()
                .streams(streams)
                .build();
    }

    @GetMapping("/manifest.json")
    @CrossOrigin
    public Manifest getManifest() {
        return addonSearchService.getManifest();
    }

    @RequestMapping(value = {"/catalog/{type}/{id}.json", "/catalog/{type}/{id}/{extra}.json"})
    @CrossOrigin
    public CatalogContainer getCatalog(@PathVariable("type") String type, @PathVariable("id") String id, @PathVariable("extra") Optional<String> extra) {
        //add logic to handle search now returns whole catalog
        return addonSearchService.getCatalog(type, id, extra);
    }


}
