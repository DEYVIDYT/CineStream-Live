package com.vplay.live;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerManager {
    private static final String TAG = "ServerManager";
    private static final String SERVERS_URL = "https://raw.githubusercontent.com/DEYVIDYT/Server_vplay/refs/heads/main/Serve.json";
    private static final String PREFS_NAME = "ServerManagerPrefs";
    private static final String PREFS_SERVERS_KEY = "servers_list";
    private static final String PREFS_CURRENT_SERVER_KEY = "current_server_index";
    
    private static volatile ServerManager instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Context context;
    private final SharedPreferences prefs;
    
    private List<Server> servers;
    private int currentServerIndex = 0;
    
    public interface ServerCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public interface ServersLoadCallback {
        void onSuccess(List<Server> servers);
        void onError(String error);
    }
    
    private ServerManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.servers = new ArrayList<>();
        
        loadCachedServers();
    }
    
    public static ServerManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    instance = new ServerManager(context);
                }
            }
        }
        return instance;
    }
    
    public void loadServers(ServersLoadCallback callback) {
        Log.d(TAG, "Loading servers from: " + SERVERS_URL);
        
        Request request = new Request.Builder()
                .url(SERVERS_URL)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch servers list", e);
                // Try to use cached servers if available
                if (!servers.isEmpty()) {
                    Log.d(TAG, "Using cached servers due to network error");
                    callback.onSuccess(servers);
                } else {
                    callback.onError("Erro ao buscar lista de servidores: " + e.getMessage());
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "HTTP error fetching servers: " + response.code());
                    if (!servers.isEmpty()) {
                        callback.onSuccess(servers);
                    } else {
                        callback.onError("Erro HTTP: " + response.code());
                    }
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG, "Received servers JSON: " + jsonData);
                    
                    Type listType = new TypeToken<List<Server>>(){}.getType();
                    List<Server> newServers = gson.fromJson(jsonData, listType);
                    
                    if (newServers != null && !newServers.isEmpty()) {
                        servers = newServers;
                        currentServerIndex = 0;
                        saveServersToCache();
                        
                        Log.d(TAG, "Successfully loaded " + servers.size() + " servers");
                        for (int i = 0; i < servers.size(); i++) {
                            Log.d(TAG, "Server " + i + ": " + servers.get(i).toString());
                        }
                        
                        callback.onSuccess(servers);
                    } else {
                        Log.w(TAG, "Empty servers list received");
                        callback.onError("Lista de servidores vazia");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing servers JSON", e);
                    if (!servers.isEmpty()) {
                        callback.onSuccess(servers);
                    } else {
                        callback.onError("Erro ao processar lista de servidores: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    public void makeRequest(String endpoint, RequestBody requestBody, ServerCallback callback) {
        makeRequest(endpoint, requestBody, callback, null);
    }
    
    public void makeRequest(String endpoint, RequestBody requestBody, ServerCallback callback, Context activityContext) {
        if (servers.isEmpty()) {
            callback.onError("Nenhum servidor disponível");
            return;
        }
        
        tryRequestWithCurrentServer(endpoint, requestBody, callback, 0, activityContext);
    }
    
    private void tryRequestWithCurrentServer(String endpoint, RequestBody requestBody, ServerCallback callback, int attemptCount, Context activityContext) {
        if (attemptCount >= servers.size()) {
            callback.onError("Todos os servidores falharam");
            return;
        }
        
        Server currentServer = servers.get(currentServerIndex);
        String url = getFullUrl(currentServer, endpoint);
        
        Log.d(TAG, "Attempting request to server " + currentServerIndex + ": " + url + " (webview: " + currentServer.isWebview() + ")");
        
        if (currentServer.isWebview()) {
            // Use activityContext for WebView requests, fallback to regular context if not provided
            Context webViewContext = activityContext != null ? activityContext : context;
            makeWebViewRequest(url, requestBody, webViewContext, new ServerCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "WebView request successful to server " + currentServerIndex);
                    callback.onSuccess(response);
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "WebView request failed to server " + currentServerIndex + ": " + error);
                    moveToNextServer();
                    tryRequestWithCurrentServer(endpoint, requestBody, callback, attemptCount + 1, activityContext);
                }
            });
        } else {
            makeHttpRequest(url, requestBody, new ServerCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "HTTP request successful to server " + currentServerIndex);
                    callback.onSuccess(response);
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "HTTP request failed to server " + currentServerIndex + ": " + error);
                    moveToNextServer();
                    tryRequestWithCurrentServer(endpoint, requestBody, callback, attemptCount + 1, activityContext);
                }
            });
        }
    }
    
    private void makeHttpRequest(String url, RequestBody requestBody, ServerCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Erro de rede: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (isValidResponse(responseBody)) {
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onError("Resposta inválida do servidor");
                    }
                } else {
                    callback.onError("Erro HTTP: " + response.code());
                }
            }
        });
    }
    
    private void makeWebViewRequest(String url, RequestBody requestBody, Context webViewContext, ServerCallback callback) {
        Log.d(TAG, "Making WebView request to: " + url);
        
        WebViewRequestHelper.makeWebViewRequest(webViewContext, url, requestBody, new WebViewRequestHelper.WebViewCallback() {
            @Override
            public void onSuccess(String response) {
                if (isValidJsonResponse(response)) {
                    callback.onSuccess(response);
                } else {
                    callback.onError("Resposta inválida do servidor");
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    private boolean isValidResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        
        // Check if response looks like valid JSON
        String trimmed = response.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }
    
    private boolean isValidJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = response.trim();
        // Check if it's valid JSON and not an error page
        return (trimmed.startsWith("{") || trimmed.startsWith("[")) && 
               !trimmed.toLowerCase().contains("<html") && 
               !trimmed.toLowerCase().contains("error") &&
               !trimmed.toLowerCase().contains("access denied");
    }
    
    private String getFullUrl(Server server, String endpoint) {
        String baseUrl = server.getUrl().endsWith("/") ? server.getUrl() : server.getUrl() + "/";
        return baseUrl + endpoint;
    }
    
    private void moveToNextServer() {
        currentServerIndex = (currentServerIndex + 1) % servers.size();
        saveCurrentServerIndex();
        Log.d(TAG, "Moved to next server: " + currentServerIndex);
    }
    
    private void loadCachedServers() {
        String serversJson = prefs.getString(PREFS_SERVERS_KEY, null);
        currentServerIndex = prefs.getInt(PREFS_CURRENT_SERVER_KEY, 0);
        
        if (serversJson != null) {
            try {
                Type listType = new TypeToken<List<Server>>(){}.getType();
                servers = gson.fromJson(serversJson, listType);
                if (servers == null) {
                    servers = new ArrayList<>();
                }
                Log.d(TAG, "Loaded " + servers.size() + " cached servers, current index: " + currentServerIndex);
            } catch (Exception e) {
                Log.e(TAG, "Error loading cached servers", e);
                servers = new ArrayList<>();
                currentServerIndex = 0;
            }
        }
    }
    
    private void saveServersToCache() {
        String serversJson = gson.toJson(servers);
        prefs.edit()
            .putString(PREFS_SERVERS_KEY, serversJson)
            .putInt(PREFS_CURRENT_SERVER_KEY, currentServerIndex)
            .apply();
        Log.d(TAG, "Saved " + servers.size() + " servers to cache");
    }
    
    private void saveCurrentServerIndex() {
        prefs.edit().putInt(PREFS_CURRENT_SERVER_KEY, currentServerIndex).apply();
    }
    
    public List<Server> getServers() {
        return new ArrayList<>(servers);
    }
    
    public Server getCurrentServer() {
        if (servers.isEmpty()) {
            return null;
        }
        return servers.get(currentServerIndex);
    }
    
    public int getCurrentServerIndex() {
        return currentServerIndex;
    }
    
    public void cleanup() {
        // Clean up any resources if needed
        Log.d(TAG, "ServerManager cleanup called");
    }
}