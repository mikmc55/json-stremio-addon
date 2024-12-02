package com.stremio.addon.service.searcher.jackett.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CapsInfo {

    @JacksonXmlProperty(localName = "server")
    private ServerInfo serverInfo;

    @JacksonXmlProperty(localName = "limits")
    private Limits limits;

    @JacksonXmlElementWrapper(localName = "searching")
    @JacksonXmlProperty(localName = "search")
    private List<SearchCapability> searchCapabilities;

    @JacksonXmlElementWrapper(localName = "categories")
    @JacksonXmlProperty(localName = "category")
    private List<Category> categories;

    @Data
    public static class ServerInfo {
        @JacksonXmlProperty(isAttribute = true)
        private String title;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Limits {
        @JacksonXmlProperty(isAttribute = true)
        private int defaultLimit;
        @JacksonXmlProperty(isAttribute = true)
        private int max;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SearchCapability {
        @JacksonXmlProperty(isAttribute = true)
        private String available;
        @JacksonXmlProperty(isAttribute = true)
        private String supportedParams;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Category {
        @JacksonXmlProperty(isAttribute = true)
        private int id;
        @JacksonXmlProperty(isAttribute = true)
        private String name;

        // Eliminamos @JacksonXmlElementWrapper para que funcione como lista de elementos "subcat"
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "subcat")
        private List<SubCategory> subCategories;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SubCategory {
        @JacksonXmlProperty(isAttribute = true)
        private int id;
        @JacksonXmlProperty(isAttribute = true)
        private String name;
    }
}
