package com.stremio.addon.service.searcher.jackett.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TorrentRss {

    @JacksonXmlProperty(localName = "channel")
    private Channel channel;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Channel {

        @JacksonXmlProperty(localName = "title")
        private String title;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "language")
        private String language;

        @JacksonXmlElementWrapper(useWrapping = false) // Desactiva el envoltorio si los elementos <item> están directamente dentro de <channel>
        @JacksonXmlProperty(localName = "item")
        private List<TorrentItem> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class TorrentItem {

        @JacksonXmlProperty(localName = "title")
        private String title;

        @JacksonXmlProperty(localName = "link")
        private String link;

        @JacksonXmlProperty(localName = "guid")
        private String guid;

        @JacksonXmlProperty(localName = "pubDate")
        private String pubDate;

        @JacksonXmlProperty(localName = "size")
        private long size;
    }
}

