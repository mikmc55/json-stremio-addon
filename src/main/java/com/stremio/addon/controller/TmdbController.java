package com.stremio.addon.controller;

import com.stremio.addon.service.FavoritesService;
import com.stremio.addon.service.tmdb.TmdbService;
import com.stremio.addon.service.tmdb.dto.MovieDetail;
import com.stremio.addon.service.tmdb.dto.PaginatedMovies;
import com.stremio.addon.service.tmdb.dto.PaginatedTvShows;
import com.stremio.addon.service.tmdb.dto.TvShowDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/tmdb", produces = "application/json")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;
    private final FavoritesService  favoritesService;

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
    @GetMapping("/tv/{tmdbId}")
    public ResponseEntity<TvShowDetail> getTvShowDetail(@PathVariable int tmdbId) {
        TvShowDetail tvShowDetail = tmdbService.getTvShowDetail(tmdbId);
        return ResponseEntity.ok(tvShowDetail);
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
            @RequestParam(defaultValue = "created_at.asc") String sortBy) {
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
}
