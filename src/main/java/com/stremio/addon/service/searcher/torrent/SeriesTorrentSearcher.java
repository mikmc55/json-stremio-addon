package com.stremio.addon.service.searcher.torrent;

import java.util.List;

public abstract class SeriesTorrentSearcher extends AbstractTorrentSearcher {

    protected abstract List<String> extractTorrentFromDetailPage(String seriesLink, String season, String episode);
}
