package com.stremio.addon.service;

import com.stremio.addon.controller.dto.Catalog;
import com.stremio.addon.controller.dto.CatalogContainer;
import com.stremio.addon.controller.dto.Manifest;
import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.mapper.TorrentMapper;
import com.stremio.addon.model.SearchEngineModel;
import com.stremio.addon.model.SearchModel;
import com.stremio.addon.model.TorrentInfoModel;
import com.stremio.addon.repository.SearchEngineRepository;
import com.stremio.addon.repository.SearchRepository;
import com.stremio.addon.service.searcher.TorrentSearcherFactory;
import com.stremio.addon.service.tmdb.TmdbService;
import com.stremio.addon.service.tmdb.dto.FindResults;
import com.stremio.addon.service.tmdb.dto.Movie;
import com.stremio.addon.service.tmdb.dto.TvShow;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final SearchEngineRepository searchEngineRepository;
    private final TorrentSearcherFactory searcherFactory;
    private final SearchRepository searchRepository;
    private final TorrentDownloaderService torrentDownloaderService;

    public List<Stream> searchMoviesTorrent(String id) {
        log.info("Searching movie torrents for ID: {}", id);
        var streams = searchRepository.findByIdentifier(id)
                .stream()
                .flatMap(searchModel -> mapToStreams(searchModel).stream())
                .collect(Collectors.toList());

        return streams.isEmpty() ? searchAndSaveTorrent("movie", id) : streams;
    }

    public List<Stream> searchSeriesTorrent(String id, String season, String episode) {
        log.info("Searching series torrents for ID: {}, Season: {}, Episode: {}", id, season, episode);
        return searchRepository.findByIdentifierAndSeasonAndEpisode(id, parseInteger(season), parseInteger(episode))
                .map(this::mapToStreams)
                .orElseGet(() -> searchAndSaveTorrent("series", id, season, episode));
    }

    @Async
    public void fetchAndSaveMovie(String id) {
        log.info("Asynchronously fetching and saving movie with ID: {}", id);
        handleFetchAndSave(id, "movie");
    }

    @Async
    public void fetchAndSaveSeries(String id, String season, String episode) {
        log.info("Asynchronously fetching and saving series with ID: {}, Season: {}, Episode: {}", id, season, episode);
        handleFetchAndSave(id, "series", season, episode);
    }

    private void handleFetchAndSave(String id, String type, String... args) {
        var searchList = searchRepository.findByIdentifier(id);
        if (searchList.isEmpty()) {
            CompletableFuture.runAsync(() -> searchAndSaveTorrent(type, id, args));
        } else {
            searchList.stream()
                    .filter(search -> search.getTorrents() == null || search.getTorrents().isEmpty())
                    .forEach(search -> CompletableFuture.runAsync(() -> searchAndSaveTorrent(type, id, args)));
        }
    }

    private List<Stream> searchAndSaveTorrent(String type, String id, String... args) {
        try {
            var result = tmdbService.findById(id, type);
            return "movie".equals(type)
                    ? processMovie(result, id, args)
                    : processTvShow(result, id, args);
        } catch (Exception e) {
            throw handleException("Error during torrent search and save", e);
        }
    }

    private List<Stream> processMovie(FindResults result, String id, String... args) {
        var movie = result.getMovieResults().stream().findFirst()
                .orElseThrow(() -> handleException("Movie not found for ID: " + id, null));
        var title = movie.getTitle();
        var year = extractYear(movie.getReleaseDate());
        var torrents = searchTorrent("movie", id, title, args);

        if (torrents.isEmpty()) {
            log.info("No torrents found for movie: [{}] [{}]", id, title);
            return List.of();
        }

        return mapToStreams(saveSearchMovies(id, title, year, torrents));
    }

    private List<Stream> processTvShow(FindResults result, String id, String... args) {
        var tvShow = result.getTvResults().stream().findFirst()
                .orElseThrow(() -> handleException("TV Show not found for ID: " + id, null));
        var title = tvShow.getName();
        var torrents = searchTorrent("series", id, title, args);

        if (torrents.isEmpty()) {
            log.info("No torrents found for series: [{}] [{}] [{}]", id, title, args);
            return List.of();
        }

        return mapToStreams(saveSearchSeries(id, title, parseInteger(args[0]), parseInteger(args[1]), torrents));
    }

    private List<String> searchTorrent(String type, String id, String title, String... args) {
        log.info("Searching torrents for type [{}] with ID [{}] and args [{}]", type, id, args);
        return searchEngineRepository.findByActive(true).parallelStream()
                .peek(provider -> log.info("Searching in provider: {}", provider.getName()))
                .flatMap(provider -> searchTorrentByProvider(title, args, type, provider).stream())
                .collect(Collectors.toList());
    }

    private List<String> searchTorrentByProvider(String title, String[] args, String type, SearchEngineModel provider) {
        try {
            var searcher = searcherFactory.getSearcher(type, provider);
            return searcher.searchTorrents(title, args);
        } catch (Exception e) {
            log.error("Error searching torrents with provider {}: {}", provider.getName(), e.getMessage());
            return List.of();
        }
    }

    private SearchModel saveSearchMovies(String id, String title, String year, List<String> torrents) {
        return saveSearch(id, title, "movie", torrents, Integer.parseInt(year), null, null);
    }

    private SearchModel saveSearchSeries(String id, String title, Integer season, Integer episode, List<String> torrents) {
        return saveSearch(id, title, "series", torrents, null, season, episode);
    }

    private SearchModel saveSearch(String id, String title, String type, List<String> torrents, Integer year, Integer season, Integer episode) {
        log.info("Saving search for {}: [{}] [{}]", type, id, title);
        try {
            return searchRepository.findByIdentifierAndSeasonAndEpisode(id, season, episode)
                    .map(search -> {
                search.setTorrents(mapToSetTorrents(torrents));
                return searchRepository.save(search);
            }).orElseGet(() -> {
                var newSearch = SearchModel.builder()
                        .identifier(id)
                        .type(type)
                        .title(title)
                        .year(year)
                        .season(season)
                        .episode(episode)
                        .torrents(mapToSetTorrents(torrents))
                        .searchTime(LocalDateTime.now())
                        .build();
                return searchRepository.save(newSearch);
            });
        } catch (Exception e) {
            throw handleException("Error saving search", e);
        }
    }

    private List<Stream> mapToStreams(SearchModel searchModel) {
        if (searchModel == null || searchModel.getTorrents() == null) {
            return List.of();
        }
        return searchModel.getTorrents().stream()
                .map(TorrentMapper.INSTANCE::map)
                .collect(Collectors.toList());
    }

    private Set<TorrentInfoModel> mapToSetTorrents(List<String> torrents) {
        return torrents.stream()
                .map(torrentDownloaderService::downloadTorrent)
                .filter(bytes -> bytes != null && bytes.length > 0)
                .map(TorrentMapper.INSTANCE::map)
                .collect(Collectors.toSet());
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw handleException("Invalid number format: " + value, e);
        }
    }

    private String extractYear(String date) {
        return date != null && date.length() >= 4 ? date.substring(0, 4) : "N/A";
    }

    private RuntimeException handleException(String message, Exception e) {
        log.error("{} - Cause: {}", message, e != null ? e.getMessage() : "Unknown");
        return new RuntimeException(message, e);
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

}
