package com.stremio.addon.service.searcher.mejortorrent;

import com.stremio.addon.service.searcher.InterfaceTorrentSearcher;

public interface InterfaceMejorTorrentSearcher extends InterfaceTorrentSearcher {
    default String getSearchPath() {
        return  "/busqueda?q=";
    }
}
