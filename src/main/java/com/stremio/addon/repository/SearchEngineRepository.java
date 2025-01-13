package com.stremio.addon.repository;

import com.stremio.addon.model.SearchEngineModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SearchEngineRepository extends CrudRepository<SearchEngineModel, Long> {
    List<SearchEngineModel> findByActive(boolean active);
}
