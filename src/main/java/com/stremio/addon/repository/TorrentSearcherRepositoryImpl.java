package com.stremio.addon.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stremio.addon.configuration.AddonConfiguration;
import com.stremio.addon.model.TorrentSearcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class TorrentSearcherRepositoryImpl implements TorrentSearcherRepository {

    private final File jsonFile;
    private final ObjectMapper objectMapper;

    public TorrentSearcherRepositoryImpl(AddonConfiguration configuration) {
        this.jsonFile = new File(configuration.getStorageDir(), "torrent_searchers.json");
        this.objectMapper = new ObjectMapper();
        log.info("Torrent searchers will be saved to: {}", jsonFile.getAbsolutePath());
    }

    @Override
    public List<TorrentSearcher> findAll() {
        log.info("Attempting to retrieve all torrent searchers from JSON file.");
        if (!jsonFile.exists()) {
            log.warn("JSON file does not exist. Returning an empty list.");
            return new ArrayList<>();
        }
        return readFromFile();
    }

    @Override
    public Optional<TorrentSearcher> findById(Long id) {
        log.info("Searching for torrent searcher with ID: {}", id);
        return findAll().stream()
                .filter(searcher -> searcher.getId().equals(id))
                .findFirst();
    }

    @Override
    public TorrentSearcher save(TorrentSearcher torrentSearcher) {
        log.info("Saving torrent searcher with ID: {}", torrentSearcher.getId());
        List<TorrentSearcher> searchers = findAll();

        if (torrentSearcher.getId() == null) {
            torrentSearcher.setId(generateNewId(searchers));
            log.info("Assigned new ID {} to torrent searcher.", torrentSearcher.getId());
        }

        searchers.removeIf(existing -> existing.getId().equals(torrentSearcher.getId()));
        searchers.add(torrentSearcher);
        writeToFile(searchers);

        log.info("Torrent searcher with ID: {} saved successfully.", torrentSearcher.getId());
        return torrentSearcher;
    }

    @Override
    public void deleteById(Long id) {
        log.info("Attempting to delete torrent searcher with ID: {}", id);
        List<TorrentSearcher> searchers = findAll();

        if (searchers.removeIf(searcher -> searcher.getId().equals(id))) {
            log.info("Torrent searcher with ID: {} removed.", id);
            writeToFile(searchers);
        } else {
            log.warn("Torrent searcher with ID: {} not found. No deletion occurred.", id);
        }
    }

    private Long generateNewId(List<TorrentSearcher> searchers) {
        return searchers.stream()
                .map(TorrentSearcher::getId)
                .max(Long::compare)
                .orElse(0L) + 1;
    }

    private List<TorrentSearcher> readFromFile() {
        return handleIOException(() -> objectMapper.readValue(jsonFile, new TypeReference<>() {}));
    }

    private void writeToFile(List<TorrentSearcher> searchers) {
        handleIOException(() -> {
            objectMapper.writeValue(jsonFile, searchers);
            return null;
        });
    }

    private <T> T handleIOException(IOExceptionSupplier<T> action) {
        try {
            return action.get();
        } catch (IOException e) {
            log.error("An error occurred while accessing the JSON file: {}", jsonFile.getAbsolutePath(), e);
            throw new RuntimeException("An error occurred while accessing the JSON file: " + jsonFile.getAbsolutePath(), e);
        }
    }

    @FunctionalInterface
    private interface IOExceptionSupplier<T> {
        T get() throws IOException;
    }
}
