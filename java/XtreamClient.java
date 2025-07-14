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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XtreamClient {
    private static final String TAG = "XtreamClient";
    private static final String CREDENTIALS_URL = "https://raw.githubusercontent.com/DEYVIDYT/CineStream-Pro/refs/heads/main/credentials_base64.txt";

    private static volatile XtreamClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private Credential currentCredential;

    public interface CredentialsCallback {
        void onSuccess(Credential credential);
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
    
    public void fetchCredentials(CredentialsCallback callback) {
        Request request = new Request.Builder()
                .url(CREDENTIALS_URL)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching credentials", e);
                callback.onError("Erro ao buscar credenciais: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String base64Data = response.body().string();
                    String decodedData = new String(Base64.decode(base64Data.trim(), Base64.DEFAULT));
                    
                    Type listType = new TypeToken<List<Credential>>(){}.getType();
                    List<Credential> credentials = gson.fromJson(decodedData, listType);
                    
                    if (credentials != null && !credentials.isEmpty()) {
                        Random random = new Random();
                        currentCredential = credentials.get(random.nextInt(credentials.size()));
                        callback.onSuccess(currentCredential);
                    } else {
                        callback.onError("Nenhuma credencial encontrada");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing credentials", e);
                    callback.onError("Erro ao processar credenciais: " + e.getMessage());
                }
            }
        });
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