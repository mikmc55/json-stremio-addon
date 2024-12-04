package com.stremio.addon.repository;

import com.stremio.addon.controller.dto.TorrentSearcher;

import java.util.List;
import java.util.Optional;


public interface TorrentSearcherRepository {
    List<TorrentSearcher> findAll();

    Optional<TorrentSearcher> findById(Long id);

    TorrentSearcher save(TorrentSearcher torrentSearcher);

    void deleteById(Long id);
}
