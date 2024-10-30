package com.stremio.addon.service.searcher.dontorrent;

import com.stremio.addon.service.searcher.InterfaceTorrentSearcher;


public interface InterfaceDonTorrentSearcher extends InterfaceTorrentSearcher {

    default String getSearchPath() {
        return "/buscar/";
    }
}
