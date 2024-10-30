package com.stremio.addon.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Catalog {
    private String type;
    private String id;
    private String name;
    private String[] genres;
    private String[] extraRequired;
    private String[] extraSupported;

}