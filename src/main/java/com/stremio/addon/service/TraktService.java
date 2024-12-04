package com.stremio.addon.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseMovie;
import com.uwetrottmann.trakt5.entities.CalendarShowEntry;
import com.uwetrottmann.trakt5.entities.TrendingShow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
public class TraktService {

    private final TraktV2 trakt;


    public TraktService(@Value("${trakt.client-id}") String clientId,
                        @Value("${trakt.client-secret}") String clientSecret,
                        @Value("${trakt.redirect-uri}") String redirectUri) {
        this.trakt = new TraktV2(clientId, clientSecret, redirectUri);
    }

    public String generateAuthorizationUrl(String redirectUri) {
        return trakt.buildAuthorizationUrl(redirectUri);
    }

    public String exchangeCodeForToken(String code) throws IOException {
        return trakt.exchangeCodeForAccessToken(code).body().access_token;
    }

    public List<TrendingShow> getTrendingShows() throws IOException {
        Response<List<TrendingShow>> response = trakt.shows().trending(1, 10, null).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException("Error retrieving data: " + response.errorBody().string());
        }
    }

    // Obtener la Watchlist
    public List<BaseMovie> getWatchlist(String accessToken) throws IOException {
        trakt.accessToken(accessToken);
        Response<List<BaseMovie>> response = trakt.sync().watchlistMovies(null).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException("Error retrieving watchlist: " + response.errorBody().string());
        }
    }

    // Obtener el Calendario
    public List<CalendarShowEntry> getCalendar(String accessToken) throws IOException {
        trakt.accessToken(accessToken);
        Response<List<CalendarShowEntry>> response = trakt.calendars().myShows(null, 7).execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException("Error retrieving calendar: " + response.errorBody().string());
        }
    }
}
