package com.cinestream.live;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class XtreamClient {
    private static final String TAG = "XtreamClient";
    private static final String AUTH_API_URL = "http://localhost:3001/auth"; // URL do nosso serviço de autenticação

    private static volatile XtreamClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private Credential currentCredential;
    private String authToken;

    public interface AuthCallback {
        void onSuccess(String token);
        void onError(String error);
    }

    public interface ChannelsCallback {
        void onSuccess(List<Channel> channels);
        void onError(String error);
    }

    public interface CategoriesCallback {
        void onSuccess(List<Category> categories);
        void onError(String error);
    }

    public XtreamClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }
    
    public void login(String email, String password, AuthCallback callback) {
        String url = AUTH_API_URL + "/login";
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error: " + response.code());
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    // Assumindo que a resposta é um JSON com um campo "access_token"
                    callback.onSuccess(responseBody);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void register(String email, String password, AuthCallback callback) {
        String url = AUTH_API_URL + "/register";
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error: " + response.code());
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    callback.onSuccess(responseBody);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public void fetchLiveStreams(ChannelsCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }
        
        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_live_streams";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching channels", e);
                callback.onError("Erro ao buscar canais: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    Type listType = new TypeToken<List<Channel>>(){}.getType();
                    List<Channel> channels = gson.fromJson(jsonData, listType);
                    
                    if (channels == null) {
                        channels = new ArrayList<>();
                    }
                    
                    // Log stream_icon for debugging
                    for (Channel channel : channels) {
                        Log.d(TAG, "Channel: " + channel.getName() + ", Icon URL: " + channel.getStream_icon());
                    }
                    
                    callback.onSuccess(channels);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing channels", e);
                    callback.onError("Erro ao processar canais: " + e.getMessage());
                }
            }
        });
    }
    
    public void fetchLiveCategories(CategoriesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }
        
        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_live_categories";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching categories", e);
                callback.onError("Erro ao buscar categorias: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    Type listType = new TypeToken<List<Category>>(){}.getType();
                    List<Category> categories = gson.fromJson(jsonData, listType);
                    
                    if (categories == null) {
                        categories = new ArrayList<>();
                    }
                    
                    callback.onSuccess(categories);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing categories", e);
                    callback.onError("Erro ao processar categorias: " + e.getMessage());
                }
            }
        });
    }
    
    public Credential getCurrentCredential() {
        return currentCredential;
    }
}