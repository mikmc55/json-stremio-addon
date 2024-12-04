package com.stremio.addon.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Data
@Table("stream")
public class StreamModel {

    @Id
    private Integer id;

    @Column("search_id")
    private Integer searchId; // Representa la clave foránea a la tabla "search".

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("info_hash")
    private String infoHash; // Clave única para este campo.

    @Column("file_name")
    private String fileName;

    @Column("video_size")
    private Long videoSize;

    @MappedCollection(idColumn = "stream_id")
    private Set<SourceModel> sources;
}
