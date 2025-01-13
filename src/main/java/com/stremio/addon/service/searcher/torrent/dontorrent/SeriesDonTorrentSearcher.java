package com.stremio.addon.service.searcher.torrent.dontorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.torrent.SeriesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("seriesDonTorrent")
@Scope("prototype")
public class SeriesDonTorrentSearcher extends SeriesTorrentSearcher implements InterfaceDonTorrentSearcher {

    @Override
    public List<String> searchTorrents(String title, String... args) {
        try {
            log.info("Starting search for torrents for series: {}", title);
            validateArgs(args);
            String season = args[0];
            String episode = args[1];
            String searchQuery = buildSearchQuery(title, season);
            String searchUrl = getSearchUrl(searchQuery);

            Document doc = invokeUrl(searchUrl);
            List<String> torrents = new ArrayList<>();

            doc.select("a.text-decoration-none").stream()
                    .filter(element -> isValidSeriesLink(element, searchQuery))
                    .forEach(element -> {
                        String seriesLink = element.attr("href");
                        torrents.addAll(extractTorrentFromDetailPage(seriesLink, season, episode));
                    });

            log.info("Search completed. Found {} torrents.", torrents.size());
            return torrents;
        } catch (Exception e) {
            throw handleException("Error occurred while searching torrents for series: " + title, e);
        }
    }

    @Override
    protected List<String> extractTorrentFromDetailPage(String seriesLink, String season, String episode) {
        try {
            log.info("Extracting episode {} of season {} from series link: {}", episode, season, seriesLink);
            String detailUrl = getUrl(seriesLink);
            Document doc = invokeUrl(detailUrl);
            String episodePattern = String.format("%sx%02d", season, Integer.parseInt(episode));

            return doc.select("tr").stream()
                    .filter(row -> isEpisodeRow(row, episodePattern))
                    .map(this::extractTorrentLink)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (Exception e) {
            throw handleException("Error occurred while extracting torrents from detail page.", e);
        }
    }

    private boolean isEpisodeRow(Element row, String episodePattern) {
        return Optional.ofNullable(row.selectFirst("td"))
                .map(td -> td.text().contains(episodePattern))
                .orElse(false);
    }

    private Optional<String> extractTorrentLink(Element row) {
        try {
            String torrentLink = row.select("a").attr("href");
            if (!torrentLink.startsWith("http")) {
                torrentLink = "http:" + torrentLink;
            }
            log.debug("Found torrent link: {}", torrentLink);
            return Optional.of(torrentLink);
        } catch (Exception e) {
            log.error("Error extracting torrent link.", e);
            return Optional.empty();
        }
    }

    private boolean isValidSeriesLink(Element element, String searchQuery) {
        try {
            String seriesTitle = element.text().trim();
            String seriesLink = element.attr("href");
            return seriesLink.startsWith("/serie") && seriesTitle.toLowerCase().startsWith(searchQuery.toLowerCase());
        } catch (Exception e) {
            log.error("Error validating series link.", e);
            return false;
        }
    }

    private String buildSearchQuery(String title, String season) {
        return normalizeText(title) + " - " + season + "ª Temporada";
    }

    private void validateArgs(String... args) {
        if (args == null || args.length < 2) {
            throw handleException("Invalid arguments. Season and episode are required.", null);
        }
    }

    private RuntimeException handleException(String message, Exception e) {
        log.error(message, e);
        return new RuntimeException(message, e);
    }
}
