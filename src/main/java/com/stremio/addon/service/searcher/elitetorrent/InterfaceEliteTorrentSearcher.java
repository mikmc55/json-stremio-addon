package com.stremio.addon.service.searcher.elitetorrent;

import com.stremio.addon.service.searcher.InterfaceTorrentSearcher;

public interface InterfaceEliteTorrentSearcher extends InterfaceTorrentSearcher {
    default String getSearchPath() {
        return  "/busqueda?q=";
    }
}
