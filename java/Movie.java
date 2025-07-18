package com.vplay.live;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    private String num;
    private String name;
    private String stream_type;
    private String stream_id;
    private String stream_icon;
    private String category_name;
    private String category_id;
    private String container_extension;
    private String added;
    private String rating;
    private String rating_5based;
    private String year;
    private String plot;
    private String cast;
    private String director;
    private String genre;
    private String duration;
    private String video_quality;
    private String tmdb_id;
    private String backdrop_path;
    private String youtube_trailer;

    public Movie() {}

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStream_type() {
        return stream_type;
    }

    public void setStream_type(String stream_type) {
        this.stream_type = stream_type;
    }

    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getContainer_extension() {
        return container_extension;
    }

    public void setContainer_extension(String container_extension) {
        this.container_extension = container_extension;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating_5based() {
        return rating_5based;
    }

    public void setRating_5based(String rating_5based) {
        this.rating_5based = rating_5based;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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

    public String getTmdb_id() {
        return tmdb_id;
    }

    public void setTmdb_id(String tmdb_id) {
        this.tmdb_id = tmdb_id;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getYoutube_trailer() {
        return youtube_trailer;
    }

    public void setYoutube_trailer(String youtube_trailer) {
        this.youtube_trailer = youtube_trailer;
    }

    public String getStreamUrl(String server, String username, String password) {
        if (stream_id != null && container_extension != null) {
            return server + "/movie/" + username + "/" + password + "/" + stream_id + "." + container_extension;
        }
        return null;
    }

    public double getRatingValue() {
        try {
            if (rating_5based != null && !rating_5based.isEmpty()) {
                return Double.parseDouble(rating_5based);
            } else if (rating != null && !rating.isEmpty()) {
                // Convert 10-based rating to 5-based
                return Double.parseDouble(rating) / 2.0;
            }
        } catch (NumberFormatException e) {
            // Return default rating
        }
        return 0.0;
    }

    protected Movie(Parcel in) {
        num = in.readString();
        name = in.readString();
        stream_type = in.readString();
        stream_id = in.readString();
        stream_icon = in.readString();
        category_name = in.readString();
        category_id = in.readString();
        container_extension = in.readString();
        added = in.readString();
        rating = in.readString();
        rating_5based = in.readString();
        year = in.readString();
        plot = in.readString();
        cast = in.readString();
        director = in.readString();
        genre = in.readString();
        duration = in.readString();
        video_quality = in.readString();
        tmdb_id = in.readString();
        backdrop_path = in.readString();
        youtube_trailer = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(num);
        dest.writeString(name);
        dest.writeString(stream_type);
        dest.writeString(stream_id);
        dest.writeString(stream_icon);
        dest.writeString(category_name);
        dest.writeString(category_id);
        dest.writeString(container_extension);
        dest.writeString(added);
        dest.writeString(rating);
        dest.writeString(rating_5based);
        dest.writeString(year);
        dest.writeString(plot);
        dest.writeString(cast);
        dest.writeString(director);
        dest.writeString(genre);
        dest.writeString(duration);
        dest.writeString(video_quality);
        dest.writeString(tmdb_id);
        dest.writeString(backdrop_path);
        dest.writeString(youtube_trailer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}