package com.stremio.addon.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class Stream {
    private String name;
    private String description;
    private String infoHash;
    private Map<String, Object> behaviorHints;
    private List<String> sources;

}
