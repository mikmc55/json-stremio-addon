package com.stremio.addon.service.searcher;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.model.TorrentSearcher;

import java.util.List;

public interface TorrentSearcherStrategy {
    void initialize(TorrentSearcher torrentSearcher);
    List<Stream> search(String title, String...args);
    String getSearchUrl(String search);
}
