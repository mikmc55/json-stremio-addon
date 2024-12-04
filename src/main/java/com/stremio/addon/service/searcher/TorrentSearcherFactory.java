package com.stremio.addon.service.searcher;

import com.stremio.addon.controller.dto.TorrentSearcher;

public interface TorrentSearcherFactory {

    TorrentSearcherStrategy getSearcher(String type, TorrentSearcher torrentSearcher);
}
