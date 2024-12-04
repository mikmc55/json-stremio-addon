package com.stremio.addon.service.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TvShowDetail {

    @JsonProperty("adult")
    private boolean adult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("created_by")
    private List<Creator> createdBy;

    @JsonProperty("episode_run_time")
    private List<Integer> episodeRunTime;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("genres")
    private List<Genre> genres;

    @JsonProperty("homepage")
    private String homepage;

    @JsonProperty("id")
    private int id;

    @JsonProperty("in_production")
    private boolean inProduction;

    @JsonProperty("languages")
    private List<String> languages;

    @JsonProperty("last_air_date")
    private String lastAirDate;

    @JsonProperty("last_episode_to_air")
    private Episode lastEpisodeToAir;

    @JsonProperty("name")
    private String name;

    @JsonProperty("next_episode_to_air")
    private Episode nextEpisodeToAir;

    @JsonProperty("networks")
    private List<Network> networks;

    @JsonProperty("number_of_episodes")
    private int numberOfEpisodes;

    @JsonProperty("number_of_seasons")
    private int numberOfSeasons;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("overview")
    private String overview;

    @JsonProperty("popularity")
    private double popularity;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("production_companies")
    private List<ProductionCompany> productionCompanies;

    @JsonProperty("production_countries")
    private List<ProductionCountry> productionCountries;

    @JsonProperty("seasons")
    private List<Season> seasons;

    @JsonProperty("spoken_languages")
    private List<SpokenLanguage> spokenLanguages;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tagline")
    private String tagline;

    @JsonProperty("type")
    private String type;

    @JsonProperty("vote_average")
    private double voteAverage;

    @JsonProperty("vote_count")
    private int voteCount;

    @Data
    public static class Creator {
        @JsonProperty("id")
        private int id;

        @JsonProperty("credit_id")
        private String creditId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("original_name")
        private String originalName;

        @JsonProperty("gender")
        private int gender;

        @JsonProperty("profile_path")
        private String profilePath;
    }

    @Data
    public static class Genre {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;
    }

    @Data
    public static class Episode {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("vote_average")
        private double voteAverage;

        @JsonProperty("vote_count")
        private int voteCount;

        @JsonProperty("air_date")
        private String airDate;

        @JsonProperty("episode_number")
        private int episodeNumber;

        @JsonProperty("episode_type")
        private String episodeType;

        @JsonProperty("production_code")
        private String productionCode;

        @JsonProperty("runtime")
        private Integer runtime;

        @JsonProperty("season_number")
        private int seasonNumber;

        @JsonProperty("show_id")
        private int showId;

        @JsonProperty("still_path")
        private String stillPath;
    }

    @Data
    public static class Network {
        @JsonProperty("id")
        private int id;

        @JsonProperty("logo_path")
        private String logoPath;

        @JsonProperty("name")
        private String name;

        @JsonProperty("origin_country")
        private String originCountry;
    }

    @Data
    public static class ProductionCompany {
        @JsonProperty("id")
        private int id;

        @JsonProperty("logo_path")
        private String logoPath;

        @JsonProperty("name")
        private String name;

        @JsonProperty("origin_country")
        private String originCountry;
    }

    @Data
    public static class ProductionCountry {
        @JsonProperty("iso_3166_1")
        private String iso31661;

        @JsonProperty("name")
        private String name;
    }

    @Data
    public static class Season {
        @JsonProperty("air_date")
        private String airDate;

        @JsonProperty("episode_count")
        private int episodeCount;

        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("season_number")
        private int seasonNumber;

        @JsonProperty("vote_average")
        private double voteAverage;
    }

    @Data
    public static class SpokenLanguage {
        @JsonProperty("english_name")
        private String englishName;

        @JsonProperty("iso_639_1")
        private String iso6391;

        @JsonProperty("name")
        private String name;
    }
}

