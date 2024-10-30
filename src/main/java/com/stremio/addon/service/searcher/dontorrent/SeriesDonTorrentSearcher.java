package com.stremio.addon.service.searcher.dontorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.SeriesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("seriesDonTorrent")
@Scope("prototype")
public class SeriesDonTorrentSearcher extends SeriesTorrentSearcher implements InterfaceDonTorrentSearcher {


    @Override
    public List<Stream> search(String title, String... args) {
        String season = args[0];
        String episode = args[1];
        String searchQuery = normalizeText(title) + " - " + season + "ª Temporada";
        String searchUrl = getSearchUrl(searchQuery);

        log.info("Searching for series: {}, Season: {}, Episode: {} at URL: {}", searchQuery, season, episode, searchUrl);

        List<Stream> streams = new ArrayList<>();
        Document doc = invokeUrl(searchUrl);

        for (Element element : doc.select("a.text-decoration-none")) {
            String seriesTitle = element.text().trim();
            String seriesLink = element.attr("href");

            log.debug("Found potential series: {}, Link: {}", seriesTitle, seriesLink);

            // Check if the element corresponds to a series
            if (seriesLink.startsWith("/serie")) {
                log.debug("Badge found: Serie");

                if (seriesTitle.toLowerCase().startsWith(searchQuery.toLowerCase())) {
                    List<String> episodeLinks = extractTorrentFromDetailPage(seriesLink, season, episode);
                    streams.addAll(generateStreams(title, episodeLinks));
                }
            }
        }

        log.info("Series search completed. Found {} torrents.", streams.size());

        return streams;
    }

    /**
     * Helper method to extract the episode torrent links from the series detail page.
     */
    @Override
    protected List<String> extractTorrentFromDetailPage(final String seriesLink, final String season, final String episode) {
        log.info("Extracting episode {} of season {} from series link: {}", episode, season, seriesLink);
        List<String> torrents = new ArrayList<>();
        String detailUrl = getUrl(seriesLink);  // Use the complete detail URL
        Document doc = invokeUrl(detailUrl);
        String episodePattern = String.format("%sx%02d", season, Integer.parseInt(episode));

        // Find the episode in the table rows
        for (Element row : doc.select("tr")) {
            if (row.selectFirst("td") != null) {
                String episodeInfo = row.selectFirst("td").text();
                if (episodeInfo.contains(episodePattern)) {
                    String torrentLink = row.select("a").attr("href");
                    if (!torrentLink.startsWith("http")) {
                        torrentLink = "http:" + torrentLink;
                    }
                    log.debug("Found episode link: {}", torrentLink);
                    torrents.add(torrentLink);
                }
            }
        }
        return torrents;
    }

}
