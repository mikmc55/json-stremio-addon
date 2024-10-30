package com.stremio.addon.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddonSearchResult {
    private List<Stream> streams;
}
