package com.stremio.addon.service.searcher;

import com.stremio.addon.model.TorrentSearcher;

public interface TorrentSearcherFactory {

    TorrentSearcherStrategy getSearcher(String type, TorrentSearcher torrentSearcher);
}
