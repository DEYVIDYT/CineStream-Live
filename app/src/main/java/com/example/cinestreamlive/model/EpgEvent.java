package com.example.cinestreamlive.model;

import android.util.Base64;
import org.json.JSONObject;
import org.json.JSONException;

public class EpgEvent {
    private String title;
    private long startEpochSeconds;
    private long endEpochSeconds;
    private String description; // Opcional
    private String channelId; // Para referência

    public EpgEvent(String title, long startEpochSeconds, long endEpochSeconds, String description, String channelId) {
        this.title = title;
        this.startEpochSeconds = startEpochSeconds;
        this.endEpochSeconds = endEpochSeconds;
        this.description = description;
        this.channelId = channelId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public long getStartEpochSeconds() {
        return startEpochSeconds;
    }

    public long getEndEpochSeconds() {
        return endEpochSeconds;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelId() {
        return channelId;
    }

    public static EpgEvent fromJson(JSONObject jsonObject) throws JSONException {
        // O título geralmente vem em Base64 na API Xtream Codes
        String titleBase64 = jsonObject.getString("title");
        String title = "";
        try {
            title = new String(Base64.decode(titleBase64, Base64.DEFAULT), "UTF-8");
        } catch (Exception e) {
            // Em caso de falha na decodificação, usar o título em Base64 ou uma string vazia
            title = titleBase64; // Ou tratar o erro de forma diferente
        }

        long startTimestamp = jsonObject.optLong("start_timestamp");
        long stopTimestamp = jsonObject.optLong("stop_timestamp");

        // A API pode retornar start/end ou start_timestamp/stop_timestamp.
        // Vamos assumir que start_timestamp e stop_timestamp são os corretos em segundos.
        // Se fossem "start" e "end" como strings de data, precisariam ser parseados.

        String descriptionBase64 = jsonObject.optString("description", "");
        String description = "";
        if (!descriptionBase64.isEmpty()) {
            try {
                description = new String(Base64.decode(descriptionBase64, Base64.DEFAULT), "UTF-8");
            } catch (Exception e) {
                description = descriptionBase64;
            }
        }
        String channelId = jsonObject.getString("channel_id");

        return new EpgEvent(title, startTimestamp, stopTimestamp, description, channelId);
    }
}
