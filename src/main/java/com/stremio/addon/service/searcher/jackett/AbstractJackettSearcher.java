package com.stremio.addon.service.searcher.jackett;

import com.stremio.addon.configuration.AddonConfiguration;
import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.model.SearchEngineModel;
import com.stremio.addon.service.searcher.AbstractStreamProcessor;
import com.stremio.addon.service.searcher.TorrentSearcherStrategy;
import com.stremio.addon.service.searcher.jackett.dto.CapsInfo;
import com.stremio.addon.service.searcher.jackett.dto.TorrentRss;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractJackettSearcher extends AbstractStreamProcessor implements TorrentSearcherStrategy {

    private final AddonConfiguration addonConfiguration;
    private final RestTemplate restTemplate;
    private SearchEngineModel torrentSearcher;

    protected AbstractJackettSearcher(AddonConfiguration addonConfiguration, RestTemplate restTemplate) {
        this.addonConfiguration = addonConfiguration;
        this.restTemplate = restTemplate;
    }

    @Override
    public void initialize(SearchEngineModel torrentSearcher) {
        this.torrentSearcher = torrentSearcher;
    }

    protected String getBaseUrl() {
        return torrentSearcher.getUrl() + "?apikey=" + addonConfiguration.getApiKey();
    }

    public String getSearchPath(String title) {
        return "&q=" + normalizeText(title);
    }

    public List<TorrentRss.TorrentItem> getTorrents(int category, String title, String season, String episode) {
        String url = buildUrl(category, title, season, episode);
        return Objects.requireNonNull(fetchAndParseXml(url, TorrentRss.class)).getChannel().getItems();
    }

    private String buildUrl(int category, String title, String season, String episode) {
        StringBuilder urlBuilder = new StringBuilder(getBaseUrl())
                .append(getSearchPath(title))
                .append(getSearchCategories(category));

        if (season != null && episode != null) {
            urlBuilder.append("&season=").append(season).append("&ep=").append(episode);
        }

        return urlBuilder.toString();
    }

    public CapsInfo getCapabilities() {
        String url = getBaseUrl() + "&t=caps";
        return fetchAndParseXml(url, CapsInfo.class);
    }

    private <T> T fetchAndParseXml(String url, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/rss+xml");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            return response.getBody();
        } catch (RestClientException e) {
            throw handleRestTemplateException(url, e);
        }
    }

    private RuntimeException handleRestTemplateException(String url, RestClientException e) {
        if (e instanceof HttpClientErrorException clientError) {
            log.error("Client error while accessing URL: {}. Status code: {}, Message: {}", url, clientError.getStatusCode(), clientError.getMessage());
        } else if (e instanceof HttpServerErrorException serverError) {
            log.error("Server error while accessing URL: {}. Status code: {}, Message: {}", url, serverError.getStatusCode(), serverError.getMessage());
        } else {
            log.error("General error while accessing URL: {}. Message: {}", url, e.getMessage(), e);
        }
        return new RuntimeException("Error accessing URL: " + url, e);
    }

    public List<Stream> searchStreams(int category, String title, String season, String episode) {
        List<TorrentRss.TorrentItem> torrentItems = getTorrents(category, title, season, episode);
        return generateStreams(torrentItems);
    }

    public List<Stream> searchStreams(int category, String title) {
        return searchStreams(category, title, null, null);
    }

    public List<String> searchTorrents(int category, String title, String season, String episode) {
        List<TorrentRss.TorrentItem> torrentItems = getTorrents(category, title, season, episode);
        return generateTorrents(title, torrentItems);
    }

    private List<String> generateTorrents(String title, List<TorrentRss.TorrentItem> torrentItems) {
        if (torrentItems == null || torrentItems.isEmpty()) {
            log.info("No torrents found for the given criteria.");
            return List.of();
        }
        return torrentItems.stream()
                .filter(torrentItem -> torrentItem.getTitle().toLowerCase().startsWith(title.toLowerCase()))
                .map(torrentItem -> {
            log.info("Torrent link found for movie: {}", torrentItem.getTitle());
            return torrentItem.getLink();
        }).collect(Collectors.toList());
    }

    public List<String> searchTorrents(int category, String title) {
        return searchTorrents(category, title, null, null);
    }

    private List<Stream> generateStreams(List<TorrentRss.TorrentItem> torrentItems) {
        if (torrentItems == null || torrentItems.isEmpty()) {
            log.info("No torrents found for the given criteria.");
            return List.of();
        }
        return torrentItems.stream().map(torrentItem -> {
            log.info("Torrent link found for movie: {}", torrentItem.getTitle());
            log.info("[{}]", torrentItem.getLink());

            return Stream.builder()
                    .name(torrentSearcher.getName())
                    .description(getFilenameFromTorrent(torrentItem.getLink()))
                    .infoHash(getInfoHashFromTorrent(torrentItem.getLink()))
                    .sources(getTrackersFromTorrent(torrentItem.getLink()))
                    .behaviorHints(getBehaviorHintsFromTorrent(torrentItem.getLink()))
                    .build();
        }).collect(Collectors.toList());
    }

    private String getSearchCategories(int categoryId) {
        String categories = getCapabilities().getCategories()
                .stream()
                .filter(category -> category.getId() == categoryId)
                .flatMap(category -> {
                    if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                        return category.getSubCategories().stream().map(CapsInfo.SubCategory::getId);
                    } else {
                        return java.util.stream.Stream.of(category.getId());
                    }
                })
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return "&cat=" + categories;
    }
}
