package com.stremio.addon.repository;

import com.stremio.addon.model.ProviderModel;
import org.springframework.data.repository.CrudRepository;

public interface ProviderRepository extends CrudRepository<ProviderModel, Long> {
}
