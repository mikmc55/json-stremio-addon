package com.stremio.addon.service.searcher.torrent.mejortorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.torrent.MoviesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("movieMejorTorrent")
@Scope("prototype")
public class MoviesMejorTorrentSearcher extends MoviesTorrentSearcher implements InterfaceMejorTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String detailPageUrl) {
        log.info("Extracting torrents from detail page: {}", detailPageUrl);

        List<String> torrents = new ArrayList<>();

        // Connect to the detail page with Jsoup and include the Referer header
        Document doc = invokeUrl(detailPageUrl);

        // Buscar el enlace de descarga con el texto "Descargar"
        Element torrentLinkElement = doc.selectFirst("a:contains(Descargar)");

        if (torrentLinkElement != null) {
            String torrentLink = torrentLinkElement.attr("href");
            if (!torrentLink.startsWith("http")) {
                torrentLink = getUrl(torrentLink); // Completa la URL si es relativa
            }
            log.info("Found torrent link: {}", torrentLink);
            torrents.add(torrentLink);
        }
        return torrents;
    }

    @Override
    public List<Stream> search(String title, String... args) {
        List<Stream> streams = new ArrayList<>();
        title = normalizeText(title);
        String searchUrl = getSearchUrl(title);
        log.info("Searching for movie: {} at URL: {}", title, searchUrl);

        Document doc = invokeUrl(searchUrl);

        for (Element element : doc.select("a:has(p.underline.text-xs.text-neutral-900)")) {
            String movieTitle = element.text().trim();
            String movieLink = element.attr("href");
            log.info("Found potential movie: {}, Link: {}", movieTitle, movieLink);

            // Verify if it's a movie based on the corresponding <span> badge
            if (movieLink.contains("/pelicula")) {
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
}
