package com.stremio.addon.service;

import com.stremio.addon.model.TorrentSearcher;
import com.stremio.addon.repository.TorrentSearcherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TorrentSearcherService {

    private final TorrentSearcherRepository repository;

    public TorrentSearcherService(TorrentSearcherRepository repository) {
        this.repository = repository;
    }

    /**
     * Save or update a searcher in the JSON file.
     */
    public TorrentSearcher saveSearcher(TorrentSearcher searcher) {
        return repository.save(searcher);
    }

    /**
     * Load searchers from JSON file, or create a new file if it doesn't exist.
     */
    public List<TorrentSearcher> getAllSearchers() {
        return repository.findAll();
    }


    /**
     * Delete a searcher from the JSON file.
     */
    public void deleteSearcher(Long id) {
        repository.deleteById(id);
    }

    public Optional<TorrentSearcher> getSearcherById(Long id) {
        return repository.findById(id);
    }
}
