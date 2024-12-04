package com.stremio.addon.service.searcher.torrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.service.searcher.AbstractStreamProcessor;
import com.stremio.addon.service.searcher.TorrentSearcherStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractTorrentSearcher extends AbstractStreamProcessor implements TorrentSearcherStrategy, InterfaceTorrentSearcher {

    private TorrentSearcher torrentSearcher;

    public AbstractTorrentSearcher() {
    }

    protected String getSearchUrl(String title) {
        try {
            log.info("Searching title [{}]", title);
            String encodedId = UriUtils.encode(title, StandardCharsets.UTF_8);
            return getUrl() + getSearchPath() + encodedId;
        } catch (Exception e) {
            log.error("Error encoding search URL for title: {}", title, e);
            throw new RuntimeException("Error encoding search URL for title: " + title, e);
        }
    }

    @Override
    public void initialize(TorrentSearcher torrentSearcher) {
        this.torrentSearcher = torrentSearcher;
    }

    protected String getUrl() {
        return torrentSearcher.getUrl();
    }

    // Método para generar la URL basada en el path
    protected String getUrl(String path) {
        try {
            String encodedId = UriUtils.encode(path, StandardCharsets.UTF_8);
            return torrentSearcher.getUrl() + "/" + encodedId;
        } catch (Exception e) {
            log.error("Error encoding URL for path: {}", path, e);
            throw new RuntimeException("Error encoding URL for path: " + path, e);
        }
    }

    // Método para invocar una URL y obtener el documento HTML con JSoup
    protected Document invokeUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .header("Referer", torrentSearcher.getUrl()) // Agregar Referer aquí
                    .get();
        } catch (Exception e) {
            log.error("Error invoking URL: {}", url, e);
            throw new RuntimeException("Error invoking URL: " + url, e);
        }
    }

    protected List<Stream> generateStreams(String title, List<String> torrentLinks) {
        List<Stream> streams = new ArrayList<>();
        if (!torrentLinks.isEmpty()) {
            for (String torrentLink : torrentLinks) {
                log.info("Torrent link found for movie: {}", title);
                log.info("[{}]", torrentLink);

                Stream stream = Stream.builder()
                        .name(torrentSearcher.getName())
                        .description(getFilenameFromTorrent(torrentLink))
                        .infoHash(getInfoHashFromTorrent(torrentLink))
                        .sources(getTrackersFromTorrent(torrentLink))
                        .behaviorHints(getBehaviorHintsFromTorrent(torrentLink))
                        .build();
                streams.add(stream);
            }
        }
        return streams;
    }

}
