package com.stremio.addon.repository;

import com.stremio.addon.model.SearchEngineModel;
import org.springframework.data.repository.CrudRepository;

public interface SearchEngineRepository extends CrudRepository<SearchEngineModel, Long> {
}
