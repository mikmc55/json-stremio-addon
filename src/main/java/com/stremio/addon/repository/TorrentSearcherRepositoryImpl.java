package com.stremio.addon.repository;

import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.mapper.SearcherMapper;
import com.stremio.addon.model.SearchEngineModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Repository
@AllArgsConstructor
public class TorrentSearcherRepositoryImpl implements TorrentSearcherRepository {

    private final SearchEngineRepository searchEngineRepository;

    @Override
    public List<TorrentSearcher> findAll() {
        Iterable<SearchEngineModel> searchEngines = searchEngineRepository.findAll();
        return StreamSupport.stream(
                searchEngines.spliterator(), false)
                .map(SearcherMapper.INSTANCE::map)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TorrentSearcher> findById(Long id) {
        log.info("Searching for torrent searcher with ID: {}", id);
        return searchEngineRepository.findById(id).map(SearcherMapper.INSTANCE::map);
    }

    @Override
    public TorrentSearcher save(TorrentSearcher torrentSearcher) {
        log.info("Saving torrent searcher with ID: {}", torrentSearcher.getId());
        searchEngineRepository.save(SearcherMapper.INSTANCE.map(torrentSearcher));

        log.info("Torrent searcher with ID: {} saved successfully.", torrentSearcher.getId());
        return torrentSearcher;
    }

    @Override
    public void deleteById(Long id) {
        log.info("Attempting to delete torrent searcher with ID: {}", id);
  		searchEngineRepository.findById(id).ifPresent(searchEngineRepository::delete);
    }

}
