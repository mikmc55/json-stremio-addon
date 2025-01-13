package com.stremio.addon.service.searcher.torrent.dontorrent;

import com.stremio.addon.controller.dto.Stream;
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
@Service("movieDonTorrent")
@Scope("prototype")
public class MoviesDonTorrentSearcher extends MoviesTorrentSearcher implements InterfaceDonTorrentSearcher {

    @Override
    public List<String> searchTorrents(String title, String... args) {
        try {
            log.info("Starting search for torrents for movie: {}", title);
            String normalizedTitle = normalizeText(title);
            String searchUrl = getSearchUrl(normalizedTitle);

            Document doc = invokeUrl(searchUrl);
            List<String> torrents = new ArrayList<>();

            doc.select("a.text-decoration-none").stream()
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

    @Override
    protected List<String> extractTorrentFromDetailPage(String detailPageUrl) {
        try {
            log.info("Extracting torrents from detail page: {}", detailPageUrl);
            String detailUrl = getUrl(detailPageUrl);

            Document doc = invokeUrl(detailUrl);
            List<String> torrents = doc.select("a.bg-primary").stream()
                    .map(this::extractTorrentLink)
                    .flatMap(Optional::stream)
                    .toList();

            log.info("Extracted {} torrents from detail page.", torrents.size());
            return torrents;
        } catch (Exception e) {
            throw handleException("Error occurred while extracting torrents from detail page.", e);
        }
    }

    private Optional<String> extractTorrentLink(Element element) {
        try {
            String torrentLink = element.attr("href");
            if (!torrentLink.startsWith("http")) {
                torrentLink = "http:" + torrentLink;
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
            return movieLink.startsWith("/pelicula") && movieTitle.toLowerCase().startsWith(normalizedTitle.toLowerCase());
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
