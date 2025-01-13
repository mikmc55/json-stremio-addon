package com.stremio.addon.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("search_engine")
public class SearchEngineModel {

    @Id
    private Integer id;

    @Column("url")
    private String url;

    @Column("name")
    private String name;

    @Column("type")
    private String type; // Se usa String para manejar el tipo ENUM como texto.

    @Column("description")
    private String description; // Mapeado para el campo TEXT.

    @Column("active")
    private boolean active;
}