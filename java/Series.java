package com.cinestream.live;

import android.os.Parcel;
import android.os.Parcelable;

public class Series implements Parcelable {
    private String num;
    private String name;
    private String title;  // Adicionado campo title
    private String year;   // Adicionado campo year
    private String series_id;
    private String cover;
    private String plot;
    private String cast;
    private String director;
    private String genre;
    private String release_date;
    private String last_modified;
    private String rating;
    private String rating_5based;
    private String tmdb_id;
    private String category_id;
    private String category_name;
    private String backdrop_path;
    private String youtube_trailer;

    public Series() {}

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
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSeries_id() {
        return series_id;
    }

    public void setSeries_id(String series_id) {
        this.series_id = series_id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(String last_modified) {
        this.last_modified = last_modified;
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

    public String getTmdb_id() {
        return tmdb_id;
    }

    public void setTmdb_id(String tmdb_id) {
        this.tmdb_id = tmdb_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
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

    protected Series(Parcel in) {
        num = in.readString();
        name = in.readString();
        title = in.readString();
        year = in.readString();
        series_id = in.readString();
        cover = in.readString();
        plot = in.readString();
        cast = in.readString();
        director = in.readString();
        genre = in.readString();
        release_date = in.readString();
        last_modified = in.readString();
        rating = in.readString();
        rating_5based = in.readString();
        tmdb_id = in.readString();
        category_id = in.readString();
        category_name = in.readString();
        backdrop_path = in.readString();
        youtube_trailer = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(num);
        dest.writeString(name);
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(series_id);
        dest.writeString(cover);
        dest.writeString(plot);
        dest.writeString(cast);
        dest.writeString(director);
        dest.writeString(genre);
        dest.writeString(release_date);
        dest.writeString(last_modified);
        dest.writeString(rating);
        dest.writeString(rating_5based);
        dest.writeString(tmdb_id);
        dest.writeString(category_id);
        dest.writeString(category_name);
        dest.writeString(backdrop_path);
        dest.writeString(youtube_trailer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Series> CREATOR = new Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel in) {
            return new Series(in);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };
}