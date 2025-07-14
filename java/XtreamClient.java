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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XtreamClient {
    private static final String TAG = "XtreamClient";
    private static volatile XtreamClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private Credential currentCredential;

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
    
    public void fetchLiveStreams(int userId, String sessionToken, ChannelsCallback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("session_token", sessionToken)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/get_live_streams.php")
                .post(formBody)
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
    
    public void fetchLiveCategories(int userId, String sessionToken, CategoriesCallback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("session_token", sessionToken)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/get_live_categories.php")
                .post(formBody)
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

    public void setCredential(Credential credential) {
        this.currentCredential = credential;
    }
}