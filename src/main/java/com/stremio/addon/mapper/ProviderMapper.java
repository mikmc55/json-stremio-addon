package com.stremio.addon.mapper;

import com.stremio.addon.controller.dto.ProviderDto;
import com.stremio.addon.model.ProviderModel;
import com.stremio.addon.service.tmdb.dto.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProviderMapper {
    static ProviderMapper INSTANCE = Mappers.getMapper(ProviderMapper.class);

    ProviderModel map(ProviderDto provider);

    ProviderDto map(ProviderModel provider);

    List<ProviderDto> map(List<Provider> providers);
}
