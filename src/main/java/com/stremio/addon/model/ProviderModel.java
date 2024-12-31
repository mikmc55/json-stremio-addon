package com.stremio.addon.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("provider")
public class ProviderModel {
    @Id
    private Long id;
    private Integer providerId;
    private String providerName;
    private String logoPath;
}
