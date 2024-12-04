package com.stremio.addon.repository;

import org.apache.el.stream.Stream;
import org.springframework.data.repository.CrudRepository;

public interface StreamRepository extends CrudRepository<Stream, Integer> {
    // Métodos personalizados si los necesitas
}

