package com.stremio.addon.service;

import com.stremio.addon.configuration.TmdbConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TmdbService {

    private final TmdbConfiguration configuration;
    private final RestTemplate restTemplate;

    @Autowired
    public TmdbService(TmdbConfiguration configuration, RestTemplate restTemplate) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieve the name of a movie or TV show from TMDB based on the IMDb ID.
     *
     * @param imdbId      The IMDb ID of the movie or TV show.
     * @param contentType The content type ('movie' or 'tv').
     * @return The name of the movie or TV show, or throws an exception if not found.
     */
    public String getTitle(String imdbId, String contentType) {
        String tmdbUrl = buildUrl(imdbId);
        HttpEntity<String> entity = createHttpEntity();

        log.info("Starting search for {} with IMDb ID: {}. URL: {}", contentType, imdbId, tmdbUrl);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(tmdbUrl,
                    org.springframework.http.HttpMethod.GET, entity, Map.class);

            return processResponse(response, contentType, imdbId);

        } catch (Exception e) {
            log.error("Error fetching title from TMDB for {} with IMDb ID {}: {}", contentType, imdbId, e.getMessage(), e);
            throw new RuntimeException("Error fetching title from TMDB for IMDb ID: " + imdbId, e);
        }
    }

    /**
     * Build the search URL for TMDB based on the IMDb ID.
     *
     * @param imdbId The IMDb ID.
     * @return The constructed URL for the search.
     */
    private String buildUrl(String imdbId) {
        String url = String.format("%s/find/%s?external_source=imdb_id&language=es",
                configuration.getApiUrl(), imdbId);
        log.debug("Constructed TMDB URL: {}", url);
        return url;
    }

    /**
     * Create the HttpEntity with the necessary headers for the TMDB request.
     *
     * @return The HttpEntity with the Authorization header.
     */
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + configuration.getApiKey());
        log.debug("Created HttpEntity with Authorization header");
        return new HttpEntity<>(headers);
    }

    /**
     * Process the response from TMDB and return the title of the movie or TV show.
     *
     * @param response    The response entity from TMDB.
     * @param contentType The content type ('movie' or 'tv').
     * @param imdbId      The IMDb ID.
     * @return The title of the movie or TV show, or throws an exception if not found.
     */
    private String processResponse(ResponseEntity<Map> response, String contentType, String imdbId) {
        Map<String, Object> responseBody = response.getBody();
        log.debug("TMDB response for {} with IMDb ID {}: {}", contentType, imdbId, responseBody);

        if (responseBody == null) {
            log.warn("Received empty response body from TMDB for IMDb ID: {}", imdbId);
            throw new RuntimeException("No response from TMDB for IMDb ID: " + imdbId);
        }

        if (contentType.equals("movie")) {
            return extractTitle(responseBody, "movie_results", "title", imdbId);
        } else if (contentType.equals("series")) {
            return extractTitle(responseBody, "tv_results", "name", imdbId);
        }

        log.warn("Invalid content type provided: {}", contentType);
        throw new RuntimeException("Invalid content type: " + contentType);
    }

    /**
     * Extract the title from the response body.
     *
     * @param responseBody The response body from TMDB.
     * @param resultKey    The key to extract the results (e.g., "movie_results" or "tv_results").
     * @param titleKey     The key to extract the title (e.g., "title" or "name").
     * @param imdbId       The IMDb ID.
     * @return The extracted title, or throws an exception if not found.
     */
    private String extractTitle(Map<String, Object> responseBody, String resultKey, String titleKey, String imdbId) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get(resultKey);

        if (results != null && !results.isEmpty()) {
            String title = (String) results.get(0).get(titleKey);
            log.info("{} found for IMDb ID {}: {}", resultKey.replace("_results", ""), imdbId, title);
            return title;
        } else {
            log.warn("No results found for IMDb ID: {}", imdbId);
            throw new RuntimeException("No results found for IMDb ID: " + imdbId);
        }
    }
}
