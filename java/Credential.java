package com.cinestream.live;

public class Credential {
    private String id;
    private String server;
    private String username;
    private String password;
    private String added_at;
    private String last_validated;

    // Add a constructor for simpler credential creation
    public Credential(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.added_at = "";
        this.last_validated = "";
    }

    public Credential(String id, String server, String username, String password, String added_at, String last_validated) {
        this.id = id;
        this.server = server;
        this.username = username;
        this.password = password;
        this.added_at = added_at;
        this.last_validated = last_validated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdded_at() {
        return added_at;
    }

    public void setAdded_at(String added_at) {
        this.added_at = added_at;
    }

    public String getLast_validated() {
        return last_validated;
    }

    public void setLast_validated(String last_validated) {
        this.last_validated = last_validated;
    }
}