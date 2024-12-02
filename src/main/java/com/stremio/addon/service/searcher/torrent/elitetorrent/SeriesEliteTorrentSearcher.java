package com.stremio.addon.service.searcher.torrent.elitetorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.torrent.SeriesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("seriesEliteTorrent")
@Scope("prototype")
public class SeriesEliteTorrentSearcher extends SeriesTorrentSearcher implements InterfaceEliteTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String seriesLink, String season, String episode) {
        return List.of();
    }

    @Override
    public List<Stream> search(String title, String... args) {
        return List.of();
    }
}
