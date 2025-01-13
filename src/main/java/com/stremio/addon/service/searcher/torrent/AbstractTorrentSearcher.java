package com.stremio.addon.service.searcher.torrent;

import com.stremio.addon.model.SearchEngineModel;
import com.stremio.addon.service.searcher.AbstractStreamProcessor;
import com.stremio.addon.service.searcher.TorrentSearcherStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AbstractTorrentSearcher extends AbstractStreamProcessor implements TorrentSearcherStrategy, InterfaceTorrentSearcher {

    private SearchEngineModel torrentSearcher;

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
    public void initialize(SearchEngineModel torrentSearcher) {
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


}
