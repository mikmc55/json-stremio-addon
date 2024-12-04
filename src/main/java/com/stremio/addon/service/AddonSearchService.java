package com.stremio.addon.service;

import com.stremio.addon.controller.dto.*;
import com.stremio.addon.mapper.StreamMapper;
import com.stremio.addon.model.SearchModel;
import com.stremio.addon.model.StreamModel;
import com.stremio.addon.repository.SearchRepository;
import com.stremio.addon.repository.TorrentSearcherRepository;
import com.stremio.addon.service.searcher.TorrentSearcherFactory;
import com.stremio.addon.service.searcher.TorrentSearcherStrategy;
import com.stremio.addon.service.tmdb.TmdbService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AddonSearchService {

    private final TmdbService tmdbService;
    private final TorrentSearcherRepository searcherRepository;
    private final TorrentSearcherFactory searcherFactory;
    private final SearchRepository searchRepository;

    public List<Stream> searchMoviesTorrent(String id) {
        log.info("Searching movie torrents for ID: {}", id);
        var searchList = searchRepository.findByIdentifier(id);

        if (!searchList.isEmpty()) {
            return searchList.stream()
                    .flatMap(searchModel -> mapStreams(searchModel).stream())
                    .toList();
        }

        return searchAndSaveTorrent("movie", id);
    }

    public List<Stream> searchSeriesTorrent(String id, String season, String episode) {
        log.info("Searching series torrents for ID: {}, Season: {}, Episode: {}", id, season, episode);
        return searchRepository.findByIdentifierAndSeasonAndEpisode(id, parseInteger(season), parseInteger(episode))
                .map(this::mapStreams)
                .orElseGet(() -> searchAndSaveTorrent("series", id, season, episode));
    }

    @Async
    public void fetchAndSaveMovie(String id) {
        log.info("Asynchronously fetching and saving movie with ID: {}", id);
        var searchList = searchRepository.findByIdentifier(id);

        if (!searchList.isEmpty()) {
            log.info("Movie already exists in database: {}", id);
            CompletableFuture.completedFuture(null);
        } else {
            CompletableFuture.runAsync(() -> searchAndSaveTorrent("movie", id));
        }

    }

    @Async
    public void fetchAndSaveSeries(String id, String season, String episode) {
        log.info("Asynchronously fetching and saving series with ID: {}, Season: {}, Episode: {}", id, season, episode);
        searchRepository.findByIdentifierAndSeasonAndEpisode(id, parseInteger(season), parseInteger(episode))
                .map(search -> {
                    log.info("Series already exists in database: {} (Season {}, Episode {})", id, season, episode);
                    return CompletableFuture.completedFuture(null); // Retorno explícito de CompletableFuture<Void>
                })
                .orElseGet(() -> {
                    CompletableFuture.runAsync(() -> searchAndSaveTorrent("series", id, season, episode));
                    return CompletableFuture.completedFuture(null); // Asegura que el tipo de retorno coincida
                });
    }

    private List<Stream> searchAndSaveTorrent(String type, String id, String... args) {
        List<Stream> streams = searchTorrent(type, id, args);
        if ("movie".equals(type)) {
            saveSearchMovies(id, streams);
        } else {
            saveSearchSeries(id, args[0], args[1], streams);
        }
        return streams;
    }

    private List<Stream> mapStreams(SearchModel searchModel) {
        return searchModel.getStreams().stream()
                .map(StreamMapper.INSTANCE::map)
                .toList();
    }

    private List<Stream> searchTorrent(String type, String id, String... args) {
        log.info("Searching torrents for type [{}] with ID [{}] and args [{}]", type, id, args);
        String title = tmdbService.getTitle(id, type);

        return searcherRepository.findAll().parallelStream()
                .peek(provider -> log.info("Searching in provider: {}", provider.getName()))
                .flatMap(provider -> searchProvider(title, args, type, provider).stream())
                .collect(Collectors.toList());
    }

    private List<Stream> searchProvider(String title, String[] args, String type, TorrentSearcher provider) {
        try {
            TorrentSearcherStrategy searcher = searcherFactory.getSearcher(type, provider);
            return searcher.search(title, args);
        } catch (Exception e) {
            log.error("Error searching torrents with provider {}: {}", provider.getName(), e.getMessage());
            return List.of();
        }
    }

    private void saveSearchMovies(String id, List<Stream> streams) {
        saveSearch(SearchModel.builder()
                .identifier(id)
                .type("movie")
                .streams(mapToSet(streams))
                .searchTime(LocalDateTime.now())
                .build());
    }

    private void saveSearchSeries(String id, String season, String episode, List<Stream> streams) {
        saveSearch(SearchModel.builder()
                .identifier(id)
                .type("series")
                .season(parseInteger(season))
                .episode(parseInteger(episode))
                .streams(mapToSet(streams))
                .searchTime(LocalDateTime.now())
                .build());
    }

    private void saveSearch(SearchModel searchModel) {
        try {
            searchRepository.save(searchModel);
            log.info("Saved search: {}", searchModel.getIdentifier());
        } catch (Exception e) {
            throw handleException("Error saving search", e);
        }
    }

    public void deleteReferences(String id) {
        try {
            log.info("Deleting references for ID {}", id);
            searchRepository.findByIdentifier(id)
                    .forEach(searchModel -> searchRepository.deleteById(searchModel.getId()));

            log.info("Successfully deleted references for ID {}", id);
        } catch (Exception e) {
            throw handleException("Error deleting references for ID: " + id, e);
        }
    }

    private Set<StreamModel> mapToSet(List<Stream> list) {
        return list.stream().map(StreamMapper.INSTANCE::map).collect(Collectors.toSet());
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw handleException("Invalid number format: " + value, e);
        }
    }

    private RuntimeException handleException(String message, Exception e) {
        if (e instanceof DbActionExecutionException) {
            log.error("{} - Cause: {}", message, e.getCause().getMessage());
            return new RuntimeException(message, e);
        }
        log.error("{} - Cause: {}", message, e.getMessage());
        return new RuntimeException(message, e);
    }

    public Manifest getManifest() {
        log.info("Returning manifest...");
        List<Catalog> catalogs = createCatalogs();
        return Manifest.builder()
                .name("Addon Spanish Torrent")
                .id("com.stremio.addon.torrent.spanish")
                .version("0.0.1")
                .description("Addon Torrent")
                .resources(new String[]{"catalog", "stream"})
                .types(new String[]{"movie", "series"})
                .catalogs(catalogs.toArray(new Catalog[0]))
                .logo("https://upload.wikimedia.org/wikipedia/de/thumb/e/e1/Java-Logo.svg/364px-Java-Logo.svg.png")
                .idPrefixes(new String[]{"tt"})
                .build();
    }

    private List<Catalog> createCatalogs() {
        return List.of(
                Catalog.builder()
                        .id("catalogTorrentMovies")
                        .type("movie")
                        .name("Torrent Movies")
                        .extraRequired(new String[]{})
                        .extraSupported(new String[]{"search"})
                        .build(),
                Catalog.builder()
                        .id("catalogTorrentSeries")
                        .type("series")
                        .name("Torrent Series")
                        .extraRequired(new String[]{})
                        .extraSupported(new String[]{"search"})
                        .build()
        );
    }

    public CatalogContainer getCatalog(String type, String id, String extra) {
        log.info("Getting [{}] in catalog [{}] with extra data [{}]", type, id, extra);
        return CatalogContainer.builder().build();
    }
}
