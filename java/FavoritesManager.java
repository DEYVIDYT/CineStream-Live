package com.cinestream.live;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREFS_NAME = "CineStreamFavorites";
    private static final String KEY_FAVORITES = "favorites";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public FavoritesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public void addFavorite(Channel channel) {
        List<Channel> favorites = getFavorites();
        
        // Check if already exists
        for (Channel fav : favorites) {
            if (fav.getStream_id() != null && fav.getStream_id().equals(channel.getStream_id())) {
                return; // Already in favorites
            }
        }
        
        favorites.add(channel);
        saveFavorites(favorites);
    }
    
    // Overloaded method for movies
    public void addFavorite(Movie movie) {
        if (movie == null || movie.getStream_id() == null) return;
        
        // Create a temporary Channel from Movie for storage
        Channel tempChannel = new Channel();
        tempChannel.setStream_id(movie.getStream_id());
        tempChannel.setName(movie.getName());
        tempChannel.setStream_icon(movie.getStream_icon());
        tempChannel.setCategory_name(movie.getCategory_name());
        tempChannel.setStream_type("movie");
        
        addFavorite(tempChannel);
    }
    
    // Overloaded method for series
    public void addFavorite(Series series) {
        if (series == null || series.getSeries_id() == null) return;
        
        // Create a temporary Channel from Series for storage
        Channel tempChannel = new Channel();
        tempChannel.setStream_id(series.getSeries_id());
        tempChannel.setName(series.getName());
        tempChannel.setStream_icon(series.getCover());
        tempChannel.setCategory_name(series.getCategory_name());
        tempChannel.setStream_type("series");
        
        addFavorite(tempChannel);
    }
    
    public void removeFavorite(Channel channel) {
        List<Channel> favorites = getFavorites();
        favorites.removeIf(fav -> fav.getStream_id() != null && fav.getStream_id().equals(channel.getStream_id()));
        saveFavorites(favorites);
    }
    
    // Overloaded method for string IDs (movies and series)
    public void removeFavorite(String streamId) {
        if (streamId == null) return;
        List<Channel> favorites = getFavorites();
        favorites.removeIf(fav -> fav.getStream_id() != null && fav.getStream_id().equals(streamId));
        saveFavorites(favorites);
    }
    
    public boolean isFavorite(Channel channel) {
        List<Channel> favorites = getFavorites();
        for (Channel fav : favorites) {
            if (fav.getStream_id() != null && fav.getStream_id().equals(channel.getStream_id())) {
                return true;
            }
        }
        return false;
    }
    
    // Overloaded method for string IDs (movies and series)
    public boolean isFavorite(String streamId) {
        if (streamId == null) return false;
        List<Channel> favorites = getFavorites();
        for (Channel fav : favorites) {
            if (fav.getStream_id() != null && fav.getStream_id().equals(streamId)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Channel> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, "[]");
        Type listType = new TypeToken<List<Channel>>(){}.getType();
        List<Channel> favorites = gson.fromJson(json, listType);
        return favorites != null ? favorites : new ArrayList<>();
    }
    
    private void saveFavorites(List<Channel> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }
    
    public void clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply();
    }
}