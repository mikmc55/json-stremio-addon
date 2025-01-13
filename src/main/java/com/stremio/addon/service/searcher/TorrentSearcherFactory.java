package com.stremio.addon.service.searcher;

import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.model.SearchEngineModel;

public interface TorrentSearcherFactory {

    TorrentSearcherStrategy getSearcher(String type, SearchEngineModel torrentSearcher);
}
