package com.stremio.addon.service.tmdb.dto;

import lombok.Data;

@Data
class EpisodeDetail {
    private String airDate;
    private int episodeNumber;
    private String name;
    private String overview;
    private int runtime;
    private double voteAverage;
    private int voteCount;
    private String stillPath;
}
