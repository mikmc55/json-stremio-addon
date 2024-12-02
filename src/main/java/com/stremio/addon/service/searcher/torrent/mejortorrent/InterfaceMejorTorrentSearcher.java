package com.stremio.addon.service.searcher.torrent.mejortorrent;

import com.stremio.addon.service.searcher.torrent.InterfaceTorrentSearcher;

public interface InterfaceMejorTorrentSearcher extends InterfaceTorrentSearcher {
    default String getSearchPath() {
        return  "/busqueda?q=";
    }
}
