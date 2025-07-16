package com.cinestream.live;

import android.os.Parcel;
import android.os.Parcelable;

public class Episode implements Parcelable {
    private String id;
    private String episode_num;
    private String title;
    private String container_extension;
    private String info;
    private String movie_image;
    private String plot;
    private String duration_secs;
    private String duration;
    private String video_quality;
    private String release_date;
    private String rating;
    private String season_num;
    private String series_id;

    public Episode() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEpisode_num() {
        return episode_num;
    }

    public void setEpisode_num(String episode_num) {
        this.episode_num = episode_num;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContainer_extension() {
        return container_extension;
    }

    public void setContainer_extension(String container_extension) {
        this.container_extension = container_extension;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getMovie_image() {
        return movie_image;
    }

    public void setMovie_image(String movie_image) {
        this.movie_image = movie_image;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getDuration_secs() {
        return duration_secs;
    }

    public void setDuration_secs(String duration_secs) {
        this.duration_secs = duration_secs;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideo_quality() {
        return video_quality;
    }

    public void setVideo_quality(String video_quality) {
        this.video_quality = video_quality;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSeason_num() {
        return season_num;
    }

    public void setSeason_num(String season_num) {
        this.season_num = season_num;
    }

    public String getSeries_id() {
        return series_id;
    }

    public void setSeries_id(String series_id) {
        this.series_id = series_id;
    }

    public String getStreamUrl(String server, String username, String password) {
        if (id != null && container_extension != null) {
            return server + "/series/" + username + "/" + password + "/" + id + "." + container_extension;
        }
        return null;
    }

    public String getFormattedEpisodeNumber() {
        if (season_num != null && episode_num != null) {
            return "S" + season_num + "E" + episode_num;
        }
        return "";
    }

    protected Episode(Parcel in) {
        id = in.readString();
        episode_num = in.readString();
        title = in.readString();
        container_extension = in.readString();
        info = in.readString();
        movie_image = in.readString();
        plot = in.readString();
        duration_secs = in.readString();
        duration = in.readString();
        video_quality = in.readString();
        release_date = in.readString();
        rating = in.readString();
        season_num = in.readString();
        series_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(episode_num);
        dest.writeString(title);
        dest.writeString(container_extension);
        dest.writeString(info);
        dest.writeString(movie_image);
        dest.writeString(plot);
        dest.writeString(duration_secs);
        dest.writeString(duration);
        dest.writeString(video_quality);
        dest.writeString(release_date);
        dest.writeString(rating);
        dest.writeString(season_num);
        dest.writeString(series_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };
}