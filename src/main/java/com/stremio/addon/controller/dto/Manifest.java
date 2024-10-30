package com.stremio.addon.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Manifest {
    private String id;
    private String version;
    private String name;
    private String description;
    private String[] idPrefixes;
    private String[] resources;
    private String[] types;
    private Catalog[] catalogs;
    private String background;
    private String logo;
    private String contactEmail;
}
