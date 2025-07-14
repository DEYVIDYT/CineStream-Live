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
    
    public void removeFavorite(Channel channel) {
        List<Channel> favorites = getFavorites();
        favorites.removeIf(fav -> fav.getStream_id() != null && fav.getStream_id().equals(channel.getStream_id()));
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