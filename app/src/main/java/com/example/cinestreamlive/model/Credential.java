package com.example.cinestreamlive.model;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.Serializable;

public class Credential implements Serializable {
    private String id;
    private String server;
    private String username;
    private String password;
    // added_at e last_validated podem ser úteis, mas não são essenciais para a lógica principal agora.
    // private String added_at;
    // private String last_validated;

    public Credential(String id, String server, String username, String password) {
        this.id = id;
        this.server = server;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static Credential fromJson(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.optString("id");
        String server = jsonObject.optString("server");
        String username = jsonObject.optString("username");
        String password = jsonObject.optString("password");
        // String added_at = jsonObject.optString("added_at");
        // String last_validated = jsonObject.optString("last_validated");
        return new Credential(id, server, username, password);
    }
}
