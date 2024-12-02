package com.stremio.addon.service.searcher.torrent;

import java.util.List;

public abstract class MoviesTorrentSearcher extends AbstractTorrentSearcher {

    protected abstract List<String> extractTorrentFromDetailPage(String detailPageUrl);
}
