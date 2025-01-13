package com.stremio.addon.repository;

import com.stremio.addon.model.SearchModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SearchRepository extends CrudRepository<SearchModel, Integer> {
    List<SearchModel> findByIdentifier(String id);

    Optional<SearchModel> findByIdentifierAndSeasonAndEpisode(String imdbId, Integer season, Integer episode);

    void deleteByIdentifier(String id);

    Optional<SearchModel> findByIdentifierAndType(String id, String movie);
}

