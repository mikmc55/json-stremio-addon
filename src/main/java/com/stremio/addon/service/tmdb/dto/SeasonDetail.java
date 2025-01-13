package com.stremio.addon.service.tmdb.dto;

import lombok.Data;

import java.util.List;

@Data
public class SeasonDetail {
    private String name;
    private String overview;
    private String airDate;
    private int seasonNumber;
    private String posterPath;
    private List<EpisodeDetail> episodes;
}
