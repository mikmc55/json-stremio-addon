package com.stremio.addon.controller.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Vector;
@Data
@Builder
public class CatalogContainer {
    Vector<MetaElement> metas = new Vector<MetaElement>();

    @Data
    @AllArgsConstructor
    class MetaElement {
        final String id;
        final String type;
        final String name;
        final String poster;
    }
}