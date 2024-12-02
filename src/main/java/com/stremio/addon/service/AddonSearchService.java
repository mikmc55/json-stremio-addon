package com.stremio.addon.service;

import com.stremio.addon.controller.dto.Catalog;
import com.stremio.addon.controller.dto.CatalogContainer;
import com.stremio.addon.controller.dto.Manifest;
import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.repository.TorrentSearcherRepository;
import com.stremio.addon.service.searcher.TorrentSearcherFactory;
import com.stremio.addon.service.searcher.TorrentSearcherStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AddonSearchService {

    @Autowired
    private TmdbService tmdbService;
    @Autowired
    private TorrentSearcherRepository searcherRepository;
    @Autowired
    private TorrentSearcherFactory searcherFactory;

    public List<Stream> searchTorrent(String type, String id, String... args) {
        String title = tmdbService.getTitle(id, type);
        return searcherRepository.findAll()
                .parallelStream()
                .peek(provider -> log.info("Searching in provider: {}", provider.getName()))
                .flatMap(provider -> {
                    try {
                        TorrentSearcherStrategy searcher = searcherFactory.getSearcher(type, provider);
                        return searcher.search(title, args).stream();
                    } catch (Exception e) {
                        log.error("Error searching torrents with provider {}: {}", provider.getName(), e.getMessage());
                        return java.util.stream.Stream.empty(); // Retorna un Stream vacío en caso de error
                    }
                })
                .collect(Collectors.toList());
    }

    public Manifest getManifest() {
        log.info("Returning manifest...");
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(Catalog.builder()
                .id("catalogTorrentMovies")
                .type("movie")
                .name("Torrent Movies")
                .genres(new String[] {})
                .extraRequired(new String[] {})
                .extraSupported(new String[] {"search"})
                .build());

        catalogs.add(Catalog.builder()
                .id("catalogTorrentSeries")
                .type("series")
                .name("Torrent Series")
                .genres(new String[] {})
                .extraRequired(new String[] {})
                .extraSupported(new String[] {"search"})
                .build());

        return Manifest.builder()
                .name("Addon Spanish Torrent")
                .id("com.stremio.addon.torrent.spanish")
                .version("0.0.1")
                .description("Addon Torrent")
                .resources(new String[]{"catalog", "stream"})
                .types(new String[]{"movie", "series"})
                .catalogs(catalogs.toArray(catalogs.toArray(new Catalog[0])))
                .logo("https://upload.wikimedia.org/wikipedia/de/thumb/e/e1/Java-Logo.svg/364px-Java-Logo.svg.png")
                .idPrefixes(new String[]{"tt"})
                .build();
    }

    public CatalogContainer getCatalog(String type, String id, Optional<String> extra) {
        log.info("Getting [{}] in catalog [{}] with extra data [{}]", type, id, extra);
        return CatalogContainer.builder().build();
    }


}
