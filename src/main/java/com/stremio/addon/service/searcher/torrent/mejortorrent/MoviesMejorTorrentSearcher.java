package com.stremio.addon.service.searcher.torrent.mejortorrent;

import com.stremio.addon.service.searcher.torrent.MoviesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("movieMejorTorrent")
@Scope("prototype")
public class MoviesMejorTorrentSearcher extends MoviesTorrentSearcher implements InterfaceMejorTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String detailPageUrl) {
        try {
            log.info("Extracting torrents from detail page: {}", detailPageUrl);
            Document doc = invokeUrl(detailPageUrl);
            return doc.select("a:contains(Descargar)").stream()
                    .map(this::extractTorrentLink)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (Exception e) {
            throw handleException("Error occurred while extracting torrents from detail page: " + detailPageUrl, e);
        }
    }

    @Override
    public List<String> searchTorrents(String title, String... args) {
        try {
            log.info("Starting search for torrents for movie: {}", title);
            String normalizedTitle = normalizeText(title);
            String searchUrl = getSearchUrl(normalizedTitle);

            Document doc = invokeUrl(searchUrl);
            List<String> torrents = new ArrayList<>();

            doc.select("a:has(p.underline.text-xs.text-neutral-900)").stream()
                    .filter(element -> isValidMovieLink(element, normalizedTitle))
                    .forEach(element -> {
                        String movieLink = element.attr("href");
                        torrents.addAll(extractTorrentFromDetailPage(movieLink));
                    });

            log.info("Search completed. Found {} torrents.", torrents.size());
            return torrents;
        } catch (Exception e) {
            throw handleException("Error occurred while searching torrents for movie: " + title, e);
        }
    }

    private Optional<String> extractTorrentLink(Element element) {
        try {
            String torrentLink = element.attr("href");
            if (!torrentLink.startsWith("http")) {
                torrentLink = getUrl() + torrentLink;
            }
            log.info("Found torrent link: {}", torrentLink);
            return Optional.of(torrentLink);
        } catch (Exception e) {
            log.error("Error extracting torrent link.", e);
            return Optional.empty();
        }
    }

    private boolean isValidMovieLink(Element element, String normalizedTitle) {
        try {
            String movieTitle = element.text().trim();
            String movieLink = element.attr("href");
            return movieLink.contains("/pelicula") && movieTitle.toLowerCase().startsWith(normalizedTitle.toLowerCase());
        } catch (Exception e) {
            log.error("Error validating movie link.", e);
            return false;
        }
    }

    private RuntimeException handleException(String message, Exception e) {
        log.error(message, e);
        return new RuntimeException(message, e);
    }
}
