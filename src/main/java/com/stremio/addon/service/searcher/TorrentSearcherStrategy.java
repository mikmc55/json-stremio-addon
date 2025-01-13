package com.stremio.addon.service.searcher;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.model.SearchEngineModel;

import java.util.List;

public interface TorrentSearcherStrategy {
    void initialize(SearchEngineModel torrentSearcher);
    List<String> searchTorrents(String title, String...args);
}
