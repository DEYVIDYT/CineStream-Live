package com.cinestream.live;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
    private String category_id;
    private String category_name;
    private String parent_id;

    public Category() {}

    public Category(String category_id, String category_name, String parent_id) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.parent_id = parent_id;
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

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    protected Category(Parcel in) {
        category_id = in.readString();
        category_name = in.readString();
        parent_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category_id);
        dest.writeString(category_name);
        dest.writeString(parent_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}