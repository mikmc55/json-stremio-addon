package com.stremio.addon.service.searcher.torrent.elitetorrent;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.service.searcher.torrent.MoviesTorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("moviesEliteTorrent")
@Scope("prototype")
public class MoviesEliteTorrentSearcher extends MoviesTorrentSearcher implements InterfaceEliteTorrentSearcher {

    @Override
    protected List<String> extractTorrentFromDetailPage(String detailPageUrl) {
        return List.of();
    }

    @Override
    public List<String> searchTorrents(String title, String... args) {
        return List.of();
    }
}
