package com.cinestream.live;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String PREFS_NAME = "CineStreamHistory";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY_SIZE = 50;
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public HistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public void addToHistory(Channel channel) {
        List<HistoryItem> history = getHistory();
        
        // Remove if already exists to avoid duplicates
        history.removeIf(item -> item.getChannel().getStream_id() != null && 
                item.getChannel().getStream_id().equals(channel.getStream_id()));
        
        // Add to beginning of list
        HistoryItem historyItem = new HistoryItem(channel, System.currentTimeMillis());
        history.add(0, historyItem);
        
        // Keep only MAX_HISTORY_SIZE items
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }
        
        saveHistory(history);
    }
    
    public List<HistoryItem> getHistory() {
        String json = prefs.getString(KEY_HISTORY, "[]");
        Type listType = new TypeToken<List<HistoryItem>>(){}.getType();
        List<HistoryItem> history = gson.fromJson(json, listType);
        return history != null ? history : new ArrayList<>();
    }
    
    public List<Channel> getHistoryChannels() {
        List<HistoryItem> history = getHistory();
        List<Channel> channels = new ArrayList<>();
        for (HistoryItem item : history) {
            channels.add(item.getChannel());
        }
        return channels;
    }
    
    private void saveHistory(List<HistoryItem> history) {
        String json = gson.toJson(history);
        prefs.edit().putString(KEY_HISTORY, json).apply();
    }
    
    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }
    
    public static class HistoryItem {
        private Channel channel;
        private long timestamp;
        
        public HistoryItem() {}
        
        public HistoryItem(Channel channel, long timestamp) {
            this.channel = channel;
            this.timestamp = timestamp;
        }
        
        public Channel getChannel() {
            return channel;
        }
        
        public void setChannel(Channel channel) {
            this.channel = channel;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getFormattedTime() {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60000) { // Less than 1 minute
                return "Agora";
            } else if (diff < 3600000) { // Less than 1 hour
                return (diff / 60000) + " min atrás";
            } else if (diff < 86400000) { // Less than 1 day
                return (diff / 3600000) + " h atrás";
            } else {
                return (diff / 86400000) + " dias atrás";
            }
        }
    }
}