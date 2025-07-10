package com.example.cinestreamlive.model;

import org.json.JSONObject;
import org.json.JSONException;

public class Channel {
    private int num;
    private String name;
    private String streamType; // "live"
    private int streamId;
    private String streamIcon; // URL do logo do canal
    private String epgChannelId;
    private String categoryId;

    public Channel(int num, String name, String streamType, int streamId, String streamIcon, String epgChannelId, String categoryId) {
        this.num = num;
        this.name = name;
        this.streamType = streamType;
        this.streamId = streamId;
        this.streamIcon = streamIcon;
        this.epgChannelId = epgChannelId;
        this.categoryId = categoryId;
    }

    // Getters
    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public String getStreamType() {
        return streamType;
    }

    public int getStreamId() {
        return streamId;
    }

    public String getStreamIcon() {
        return streamIcon;
    }

    public String getEpgChannelId() {
        return epgChannelId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public static Channel fromJson(JSONObject jsonObject) throws JSONException {
        int num = jsonObject.optInt("num");
        String name = jsonObject.optString("name");
        String streamType = jsonObject.optString("stream_type");
        int streamId = jsonObject.optInt("stream_id");
        String streamIcon = jsonObject.optString("stream_icon");
        String epgChannelId = jsonObject.optString("epg_channel_id");
        String categoryId = jsonObject.optString("category_id"); // Ou pode ser int dependendo da API

        return new Channel(num, name, streamType, streamId, streamIcon, epgChannelId, categoryId);
    }
}
