package com.stremio.addon.service.searcher.torrent.dontorrent;

import com.stremio.addon.service.searcher.torrent.InterfaceTorrentSearcher;


public interface InterfaceDonTorrentSearcher extends InterfaceTorrentSearcher {

    default String getSearchPath() {
        return "/buscar/";
    }
}
