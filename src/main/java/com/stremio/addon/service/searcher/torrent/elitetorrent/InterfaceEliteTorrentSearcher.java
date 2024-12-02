package com.stremio.addon.service.searcher.torrent.elitetorrent;

import com.stremio.addon.service.searcher.torrent.InterfaceTorrentSearcher;

public interface InterfaceEliteTorrentSearcher extends InterfaceTorrentSearcher {
    default String getSearchPath() {
        return  "/busqueda?q=";
    }
}
