package com.stremio.addon.service.searcher.dontorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.MoviesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("movieDonTorrent")
@Scope("prototype")
public class MoviesDonTorrentSearcher extends MoviesTorrentSearcher implements InterfaceDonTorrentSearcher {

    // Método para generar la URL de búsqueda basada en el título


    @Override
    public List<Stream> search(String title, String... args) {
        List<Stream> streams = new ArrayList<>();
        title = normalizeText(title);
        String searchUrl = getSearchUrl(title);
        log.info("Searching for movie: {} at URL: {}", title, searchUrl);

        Document doc = invokeUrl(searchUrl);

        for (Element element : doc.select("a.text-decoration-none")) {
            String movieTitle = element.text().trim();
            String movieLink = element.attr("href");
            log.info("Found potential movie: {}, Link: {}", movieTitle, movieLink);

            // Verify if it's a movie based on the corresponding <span> badge
            if (movieLink.startsWith("/pelicula")) {
                log.info("Badge found: {}", movieLink);

                // Case-insensitive comparison of titles
                if (movieTitle.toLowerCase().startsWith(title.toLowerCase())) {
                    List<String> torrentLinks = extractTorrentFromDetailPage(movieLink);
                    streams.addAll(generateStreams(title, torrentLinks));
                }
            }
        }
        log.info("Movie search completed. Found {} torrents.", streams.size());

        return streams;
    }

    @Override
    protected List<String> extractTorrentFromDetailPage(final String detailPageUrl) {
        log.info("Extracting torrents from detail page: {}", detailPageUrl);

        List<String> torrents = new ArrayList<>();
        String detailUrl = getUrl(detailPageUrl);

        // Connect to the detail page with Jsoup and include the Referer header
        Document doc = invokeUrl(detailUrl);

        for (Element element : doc.select("a.bg-primary")) {
            String torrentLink = element.attr("href");

            // Ensure the link has the correct scheme
            if (!torrentLink.startsWith("http")) {
                torrentLink = "http:" + torrentLink;
            }

            log.info("Found torrent link: {}", torrentLink);
            torrents.add(torrentLink);
        }

        log.info("Extracted {} torrents from detail page.", torrents.size());

        return torrents;
    }

}
