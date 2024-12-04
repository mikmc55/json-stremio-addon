package com.stremio.addon.mapper;

import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.model.SearchEngineModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SearcherMapper {
    static SearcherMapper INSTANCE = Mappers.getMapper(SearcherMapper.class);

    TorrentSearcher map(SearchEngineModel model);

    SearchEngineModel map(TorrentSearcher torrentSearcher);
}
