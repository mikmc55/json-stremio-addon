package com.stremio.addon.service.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FindResults {
    @JsonProperty("movie_results")
    private List<Movie> movieResults;

    @JsonProperty("tv_results")
    private List<TvShow> tvResults;
}
