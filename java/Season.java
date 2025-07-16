package com.vplay.live;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Season implements Parcelable {
    private String seasonNumber;
    private String seasonName;
    private List<Episode> episodes;
    private int totalEpisodes;
    
    public Season() {
        this.episodes = new ArrayList<>();
    }
    
    public Season(String seasonNumber) {
        this.seasonNumber = seasonNumber;
        this.episodes = new ArrayList<>();
        this.seasonName = "Temporada " + seasonNumber;
    }
    
    public Season(String seasonNumber, String seasonName) {
        this.seasonNumber = seasonNumber;
        this.seasonName = seasonName;
        this.episodes = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getSeasonNumber() {
        return seasonNumber;
    }
    
    public void setSeasonNumber(String seasonNumber) {
        this.seasonNumber = seasonNumber;
    }
    
    public String getSeasonName() {
        return seasonName != null ? seasonName : "Temporada " + seasonNumber;
    }
    
    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }
    
    public List<Episode> getEpisodes() {
        return episodes;
    }
    
    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes != null ? episodes : new ArrayList<>();
        this.totalEpisodes = this.episodes.size();
    }
    
    public void addEpisode(Episode episode) {
        if (episodes == null) {
            episodes = new ArrayList<>();
        }
        episodes.add(episode);
        this.totalEpisodes = episodes.size();
    }
    
    public int getTotalEpisodes() {
        return episodes != null ? episodes.size() : 0;
    }
    
    public Episode getFirstEpisode() {
        return episodes != null && !episodes.isEmpty() ? episodes.get(0) : null;
    }
    
    public String getFormattedSeasonInfo() {
        int count = getTotalEpisodes();
        return getSeasonName() + " (" + count + (count == 1 ? " episódio)" : " episódios)");
    }
    
    // Parcelable implementation
    protected Season(Parcel in) {
        seasonNumber = in.readString();
        seasonName = in.readString();
        episodes = in.createTypedArrayList(Episode.CREATOR);
        totalEpisodes = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(seasonNumber);
        dest.writeString(seasonName);
        dest.writeTypedList(episodes);
        dest.writeInt(totalEpisodes);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Season> CREATOR = new Creator<Season>() {
        @Override
        public Season createFromParcel(Parcel in) {
            return new Season(in);
        }
        
        @Override
        public Season[] newArray(int size) {
            return new Season[size];
        }
    };
}