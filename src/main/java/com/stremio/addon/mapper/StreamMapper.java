package com.stremio.addon.mapper;

import com.stremio.addon.controller.dto.Stream;
import com.stremio.addon.model.SourceModel;
import com.stremio.addon.model.StreamModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface StreamMapper {
    StreamMapper INSTANCE = Mappers.getMapper(StreamMapper.class);

    @Mapping(source = "model", target = "behaviorHints", qualifiedByName = "getBehaviorHints")
    Stream map(StreamModel model);

    List<String> map(Set<SourceModel> value);

    default String map(SourceModel value) {
        return value.getTracker();
    }

    @Named("getBehaviorHints")
    default Map<String, Object> getBehaviorHints(StreamModel model) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fileName", model.getFileName());
        map.put("videoSize", model.getVideoSize());
        return map;
    }

    @Mapping(source="stream", target = "fileName", qualifiedByName = "getFileName")
    @Mapping(source="stream", target = "videoSize", qualifiedByName = "getVideoSize")
    StreamModel map(Stream stream);

    default Set<SourceModel> map(List<String> sources) {
        return sources.stream().map(source -> SourceModel.builder().tracker(source).build())
                .collect(Collectors.toSet());
    }

    @Named("getFileName")
    default String getFileName(Stream stream) {
        return stream.getBehaviorHints().get("filename").toString();
    }

    @Named("getVideoSize")
    default String getVideoSize(Stream stream) {
        return stream.getBehaviorHints().get("videoSize").toString();
    }
}
