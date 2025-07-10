package com.example.cinestreamlive.model;

import org.json.JSONObject;
import org.json.JSONException;

public class Channel {
    private int num;
    private String name;
    private String streamType; // "live"
    private int streamId;
    private String streamIcon; // URL do logo do canal
    private String epgChannelId; // Este é o ID usado para mapear com dados de EPG
    private String categoryId;
    private EpgEvent currentEpgEvent; // Para armazenar o evento EPG atual

    public Channel(int num, String name, String streamType, int streamId, String streamIcon, String epgChannelId, String categoryId) {
        this.num = num;
        this.name = name;
        this.streamType = streamType;
        this.streamId = streamId;
        this.streamIcon = streamIcon;
        this.epgChannelId = epgChannelId; // Importante para EPG
        this.categoryId = categoryId;
        this.currentEpgEvent = null; // Inicializa como null
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

    public EpgEvent getCurrentEpgEvent() {
        return currentEpgEvent;
    }

    // Setter
    public void setCurrentEpgEvent(EpgEvent currentEpgEvent) {
        this.currentEpgEvent = currentEpgEvent;
    }

    public static Channel fromJson(JSONObject jsonObject) throws JSONException {
        int num = jsonObject.optInt("num");
        String name = jsonObject.optString("name");
        String streamType = jsonObject.optString("stream_type");
        int streamId = jsonObject.optInt("stream_id"); // Usado para construir URL de stream
        String streamIcon = jsonObject.optString("stream_icon");
        // 'epg_channel_id' é o ID que a API usa nos listings de EPG. Pode ser diferente do stream_id.
        String epgChannelId = jsonObject.optString("epg_channel_id");
        if (epgChannelId == null || epgChannelId.isEmpty() || "null".equalsIgnoreCase(epgChannelId)) {
            // Fallback: algumas APIs usam o stream_id como epg_channel_id se não for fornecido explicitamente
            epgChannelId = String.valueOf(streamId);
        }
        String categoryId = jsonObject.optString("category_id");

        return new Channel(num, name, streamType, streamId, streamIcon, epgChannelId, categoryId);
    }
}
