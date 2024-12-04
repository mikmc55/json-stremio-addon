package com.stremio.addon.repository;

import com.stremio.addon.model.SourceModel;
import org.springframework.data.repository.CrudRepository;

public interface SourceRepository extends CrudRepository<SourceModel, Integer> {
    // Métodos personalizados si los necesitas.
}
