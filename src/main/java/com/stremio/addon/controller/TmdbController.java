package com.stremio.addon.controller;

import com.stremio.addon.controller.dto.ProviderDto;
import com.stremio.addon.service.FavoritesService;
import com.stremio.addon.service.tmdb.TmdbService;
import com.stremio.addon.service.tmdb.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/tmdb", produces = "application/json")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;
    private final FavoritesService favoritesService;

    /**
     * Get the title of a movie or TV show by IMDb ID.
     */
    @GetMapping("/title")
    public ResponseEntity<String> getTitle(
            @RequestParam String imdbId,
            @RequestParam String contentType) {
        String title = tmdbService.getTitle(imdbId, contentType);
        return ResponseEntity.ok(title);
    }

    /**
     * Get details of a movie by TMDB ID.
     */
    @GetMapping("/movie/{tmdbId}")
    public ResponseEntity<MovieDetail> getMovieDetail(@PathVariable int tmdbId) {
        MovieDetail movieDetail = tmdbService.getMovieDetail(tmdbId);
        return ResponseEntity.ok(movieDetail);
    }

    /**
     * Get details of a TV show by TMDB ID.
     */
    @GetMapping("/tv/{tvShowId}")
    public ResponseEntity<TvShowDetail> getTvShowDetail(@PathVariable int tvShowId) {
        TvShowDetail tvShowDetail = tmdbService.getTvShowDetail(tvShowId);
        return ResponseEntity.ok(tvShowDetail);
    }

    /**
     * Get details of all episodes in a specific season of a TV show.
     *
     * @param tvShowId    The TMDB ID of the TV show.
     * @param seasonNumber The season number to fetch.
     * @return Details of the episodes in the season.
     */
    @GetMapping("/tv/{tvShowId}/season/{seasonNumber}/episodes")
    public ResponseEntity<SeasonDetail> getSeasonDetails(
            @PathVariable int tvShowId,
            @PathVariable int seasonNumber) {
        SeasonDetail seasonDetail = tmdbService.getSeasonDetails(tvShowId, seasonNumber);
        return ResponseEntity.ok(seasonDetail);
    }

    /**
     * Get trending movies with pagination.
     */
    @GetMapping("/trending/movies")
    public ResponseEntity<PaginatedMovies> getTrendingMovies(@RequestParam(defaultValue = "1") int page) {
        PaginatedMovies trendingMovies = tmdbService.getTrendingMovies(page);
        return ResponseEntity.ok(trendingMovies);
    }

    /**
     * Get trending TV shows with pagination.
     */
    @GetMapping("/trending/tv")
    public ResponseEntity<PaginatedTvShows> getTrendingTvShows(@RequestParam(defaultValue = "1") int page) {
        PaginatedTvShows trendingTvShows = tmdbService.getTrendingTvShows(page);
        return ResponseEntity.ok(trendingTvShows);
    }

    /**
     * Get favorite movies with pagination and sorting.
     */
    @GetMapping("/favorites/movies")
    public ResponseEntity<PaginatedMovies> getFavoriteMovies(
            @RequestParam int page,
            @RequestParam(defaultValue = "popularity.desc") String sortBy) {
        PaginatedMovies favoriteMovies = tmdbService.getFavoriteMovies(page, sortBy);
        return ResponseEntity.ok(favoriteMovies);
    }

    /**
     * Get favorite TV shows with pagination and sorting.
     */
    @GetMapping("/favorites/tv")
    public ResponseEntity<PaginatedTvShows> getFavoriteTvShows(
            @RequestParam int page,
            @RequestParam(defaultValue = "created_at.asc") String sortBy) {
        PaginatedTvShows favoriteTvShows = tmdbService.getFavoriteTvShows(page, sortBy);
        return ResponseEntity.ok(favoriteTvShows);
    }

    /**
     * Mark or unmark a movie or TV show as a favorite.
     */
    @PostMapping("/favorites")
    public ResponseEntity<Void> markAsFavorite(
            @RequestParam long mediaId,
            @RequestParam String mediaType,
            @RequestParam boolean favorite) {
        favoritesService.manageFavorite(mediaId, mediaType, favorite);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para buscar películas con filtros avanzados.
     *
     * @param filters Filtros personalizados de búsqueda (opcional).
     * @return Lista paginada de películas que coinciden con los filtros.
     */
    @GetMapping("/discover/movies")
    public ResponseEntity<PaginatedMovies> discoverMovies(
            @RequestParam Map<String, String> filters) {
        PaginatedMovies movies = tmdbService.discoverMovies(filters);
        return ResponseEntity.ok(movies);
    }

    /**
     * Endpoint para buscar series con filtros avanzados.
     *
     * @param filters Filtros personalizados de búsqueda (opcional).
     * @return Lista paginada de series que coinciden con los filtros.
     */
    @GetMapping("/discover/tv")
    public ResponseEntity<PaginatedTvShows> discoverTvShows(
            @RequestParam Map<String, String> filters) {
        PaginatedTvShows tvShows = tmdbService.discoverTvShows(filters);
        return ResponseEntity.ok(tvShows);
    }

    @GetMapping("/watch/providers/tv")
    public ResponseEntity<?> getTvWatchProviders() {
        ProvidersResponse response = tmdbService.getTvWatchProviders();
        return ResponseEntity.ok(response.getResults());
    }

    @GetMapping("/watch/providers/movies")
    public ResponseEntity<?> getMovieProviders() {
        ProvidersResponse response = tmdbService.getMovieProviders();
        return ResponseEntity.ok(response.getResults());
    }

    @GetMapping("{tv}/watch/providers/tv")
    public ResponseEntity<?> getTvWatchProviders(@PathVariable("tv") Integer id) {
        return ResponseEntity.ok(tmdbService.getTvWatchProviders(id));
    }

    @GetMapping("{movie}/watch/providers/movie")
    public ResponseEntity<?> getMovieProviders(@PathVariable("movie") Integer movieId) {
        return ResponseEntity.ok(tmdbService.getMovieWatchProviders(movieId));
    }

    @GetMapping("/providers/subscribe")
    public ResponseEntity<List<ProviderDto>> getProviders() {
        return ResponseEntity.ok(tmdbService.getUserProviders());
    }

    @PostMapping("/providers/subscribe")
    public ResponseEntity<String> saveUserProviders(@RequestBody List<ProviderDto> request) {
        tmdbService.saveUserProviders(request);
        return ResponseEntity.ok("Proveedores guardados exitosamente");
    }

    /**
     * Buscar películas o series por título.
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedSearchResults> searchMoviesOrSeries(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        PaginatedSearchResults searchResults = tmdbService.search(query, page);
        return ResponseEntity.ok(searchResults);
    }
}
