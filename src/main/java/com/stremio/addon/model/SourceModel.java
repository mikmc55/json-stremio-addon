package com.stremio.addon.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("source")
@Builder
public class SourceModel {

    @Id
    private Integer id;

    @Column("tracker")
    private String tracker;

    @Column("stream_id")
    private Integer streamId; // Representa la clave foránea a la tabla "stream".

}

