package com.stremio.addon.controller;

import com.stremio.addon.service.TraktService;
import com.uwetrottmann.trakt5.entities.BaseMovie;
import com.uwetrottmann.trakt5.entities.CalendarShowEntry;
import com.uwetrottmann.trakt5.entities.TrendingShow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/trakt", produces = "application/json")
public class TraktController {

    private final TraktService traktService;

    @Autowired
    public TraktController(TraktService traktService) {
        this.traktService = traktService;
    }

    @GetMapping(path = "/trending-shows", produces = "application/json")
    public List<TrendingShow> getTrendingShows() throws IOException {
        return traktService.getTrendingShows();
    }

    // Endpoint para obtener la Watchlist
    @GetMapping("/watchlist")
    public List<BaseMovie> getWatchlist(@RequestHeader("Authorization") String accessToken) throws IOException {
        String token = accessToken.replace("Bearer ", "");
        return traktService.getWatchlist(token);
    }

    // Endpoint para obtener el Calendario
    @GetMapping("/calendar")
    public List<CalendarShowEntry> getCalendar(@RequestHeader("Authorization") String accessToken) throws IOException {
        String token = accessToken.replace("Bearer ", "");
        return traktService.getCalendar(token);
    }
}

