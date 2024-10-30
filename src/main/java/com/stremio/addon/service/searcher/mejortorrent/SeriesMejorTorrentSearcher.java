package com.stremio.addon.service.searcher.mejortorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.SeriesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("seriesMejorTorrent")
@Scope("prototype")
public class SeriesMejorTorrentSearcher extends SeriesTorrentSearcher implements InterfaceMejorTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String seriesLink, String season, String episode) {
        log.info("Extracting episode {} of season {} from series link: {}", episode, season, seriesLink);
  // Use the complete detail URL
        Document doc = invokeUrl(seriesLink);
        String episodePattern = String.format("%sx%02d", season, Integer.parseInt(episode));

        // Selecciona todos los episodios de la tabla
        Elements rows = doc.select("table tbody tr");

        List<String> torrentLinks = new ArrayList<>();
        for (Element row : rows) {
            String episodeInfo = row.select("td:nth-child(2)").text();

            // Verifica si el episodio coincide con el número que estamos buscando
            if (episodeInfo.equals(episodePattern)) {
                // Extrae el enlace de descarga
                Element downloadLinkElement = row.select("td a[target=_blank]").first();
                if (downloadLinkElement != null) {
                    String torrentLink = downloadLinkElement.attr("href");
                    // Si el enlace es relativo, le agrega la URL base
                    if (!torrentLink.startsWith("http")) {
                        torrentLink = getUrl(torrentLink);
                    }
                    torrentLinks.add(torrentLink);
                }
            }
        }
        return torrentLinks;
    }

    @Override
    public List<Stream> search(String title, String... args) {
        List<Stream> streams = new ArrayList<>();
        String season = args[0];
        String episode = args[1];
        String searchQuery = normalizeText(title) + " - " + season + "ª Temporada";
        String searchUrl = getSearchUrl(searchQuery);
        log.info("Searching for movie: {} at URL: {}", title, searchUrl);

        Document doc = invokeUrl(searchUrl);

        for (Element element : doc.select("a:has(p.underline.text-xs.text-neutral-900)")) {
            String serieTitle = element.text().trim();
            String serieLink = element.attr("href");
            log.info("Found potential movie: {}, Link: {}", serieTitle, serieLink);

            // Verify if it's a movie based on the corresponding <span> badge
            if (serieLink.contains("/serie")) {
                log.info("Badge found: {}", serieLink);

                // Case-insensitive comparison of titles
                if (serieTitle.toLowerCase().startsWith(title.toLowerCase())) {
                    List<String> torrentLinks = extractTorrentFromDetailPage(serieLink, season, episode);
                    streams.addAll(generateStreams(title, torrentLinks));
                }
            }
        }
        log.info("Movie search completed. Found {} torrents.", streams.size());

        return streams;
    }
}
