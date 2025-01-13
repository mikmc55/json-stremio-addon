package com.stremio.addon.service.searcher.torrent.mejortorrent;

import com.stremio.addon.service.searcher.torrent.SeriesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("seriesMejorTorrent")
@Scope("prototype")
public class SeriesMejorTorrentSearcher extends SeriesTorrentSearcher implements InterfaceMejorTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String seriesLink, String season, String episode) {
        try {
            log.info("Extracting episode {} of season {} from series link: {}", episode, season, seriesLink);
            Document doc = invokeUrl(seriesLink);
            String episodePattern = String.format("%sx%02d", season, Integer.parseInt(episode));
            Elements rows = doc.select("table tbody tr");

            return rows.stream()
                    .filter(row -> row.select("td:nth-child(2)").text().equals(episodePattern))
                    .map(this::extractTorrentLink)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (Exception e) {
            throw handleException("Error occurred while extracting torrents from detail page.", e);
        }
    }

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

            doc.select("a:has(p.underline.text-xs.text-neutral-900)").stream()
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

    private Optional<String> extractTorrentLink(Element row) {
        try {
            Element downloadLinkElement = row.select("td a[target=_blank]").first();
            if (downloadLinkElement != null) {
                String torrentLink = downloadLinkElement.attr("href");
                if (!torrentLink.startsWith("http")) {
                    torrentLink = getUrl() + torrentLink;
                }
                log.debug("Found torrent link: {}", torrentLink);
                return Optional.of(torrentLink);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error extracting torrent link.", e);
            return Optional.empty();
        }
    }

    private boolean isValidSeriesLink(Element element, String searchQuery) {
        try {
            String seriesTitle = element.text().trim();
            String seriesLink = element.attr("href");
            return seriesLink.contains("/serie") && seriesTitle.toLowerCase().startsWith(searchQuery.toLowerCase());
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
