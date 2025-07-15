package com.cinestream.live;

import android.os.Parcel;
import android.os.Parcelable;

public class Server implements Parcelable {
    private String url;
    private boolean webview;
    
    public Server() {}
    
    public Server(String url, boolean webview) {
        this.url = url;
        this.webview = webview;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public boolean isWebview() {
        return webview;
    }
    
    public void setWebview(boolean webview) {
        this.webview = webview;
    }
    
    public String getLoginUrl() {
        String baseUrl = url.endsWith("/") ? url : url + "/";
        return baseUrl + "login.php";
    }
    
    public String getRegisterUrl() {
        String baseUrl = url.endsWith("/") ? url : url + "/";
        return baseUrl + "register.php";
    }
    
    public String getProfileUrl() {
        String baseUrl = url.endsWith("/") ? url : url + "/";
        return baseUrl + "profile.php";
    }
    
    protected Server(Parcel in) {
        url = in.readString();
        webview = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeByte((byte) (webview ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Server> CREATOR = new Creator<Server>() {
        @Override
        public Server createFromParcel(Parcel in) {
            return new Server(in);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };
    
    @Override
    public String toString() {
        return "Server{" +
                "url='" + url + '\'' +
                ", webview=" + webview +
                '}';
    }
}