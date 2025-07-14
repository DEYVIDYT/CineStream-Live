package com.cinestream.live;

import android.util.Base64;
import android.util.Log;

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

    public interface MoviesCallback {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }

    public interface SeriesCallback {
        void onSuccess(List<Series> series);
        void onError(String error);
    }

    public interface EpisodesCallback {
        void onSuccess(List<Episode> episodes);
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

    public void setCredential(Credential credential) {
        this.currentCredential = credential;
        if (credential != null) {
            Log.d(TAG, "Credential set: " + credential.getServer());
        } else {
            Log.d(TAG, "Credential set to null");
        }
    }

    public void fetchVodCategories(CategoriesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }

        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_vod_categories";

        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching VOD categories", e);
                callback.onError("Erro ao buscar categorias de filmes: " + e.getMessage());
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
                    Log.e(TAG, "Error parsing VOD categories", e);
                    callback.onError("Erro ao processar categorias de filmes: " + e.getMessage());
                }
            }
        });
    }

    public void fetchVodStreams(MoviesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }

        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_vod_streams";

        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching movies", e);
                callback.onError("Erro ao buscar filmes: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    Type listType = new TypeToken<List<Movie>>(){}.getType();
                    List<Movie> movies = gson.fromJson(jsonData, listType);
                    
                    if (movies == null) {
                        movies = new ArrayList<>();
                    }
                    
                    // Log movie info for debugging
                    for (Movie movie : movies) {
                        Log.d(TAG, "Movie: " + movie.getName() + ", Icon URL: " + movie.getStream_icon());
                    }
                    
                    callback.onSuccess(movies);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing movies", e);
                    callback.onError("Erro ao processar filmes: " + e.getMessage());
                }
            }
        });
    }

    public void fetchSeriesCategories(CategoriesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }

        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_series_categories";

        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching series categories", e);
                callback.onError("Erro ao buscar categorias de séries: " + e.getMessage());
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
                    Log.e(TAG, "Error parsing series categories", e);
                    callback.onError("Erro ao processar categorias de séries: " + e.getMessage());
                }
            }
        });
    }

    public void fetchSeries(SeriesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }

        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_series";

        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching series", e);
                callback.onError("Erro ao buscar séries: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    Type listType = new TypeToken<List<Series>>(){}.getType();
                    List<Series> series = gson.fromJson(jsonData, listType);
                    
                    if (series == null) {
                        series = new ArrayList<>();
                    }
                    
                    // Log series info for debugging
                    for (Series s : series) {
                        Log.d(TAG, "Series: " + s.getName() + ", Cover URL: " + s.getCover());
                    }
                    
                    callback.onSuccess(series);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing series", e);
                    callback.onError("Erro ao processar séries: " + e.getMessage());
                }
            }
        });
    }

    public void fetchSeriesInfo(String seriesId, EpisodesCallback callback) {
        if (currentCredential == null) {
            callback.onError("Credenciais não carregadas");
            return;
        }

        String url = currentCredential.getServer() + "/player_api.php" +
                "?username=" + currentCredential.getUsername() +
                "&password=" + currentCredential.getPassword() +
                "&action=get_series_info" +
                "&series_id=" + seriesId;

        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching series info", e);
                callback.onError("Erro ao buscar informações da série: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    // Parse the complex series info JSON structure
                    org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
                    List<Episode> allEpisodes = new ArrayList<>();
                    
                    if (jsonObject.has("episodes")) {
                        org.json.JSONObject episodes = jsonObject.getJSONObject("episodes");
                        java.util.Iterator<String> seasonKeys = episodes.keys();
                        
                        while (seasonKeys.hasNext()) {
                            String seasonNum = seasonKeys.next();
                            org.json.JSONArray seasonEpisodes = episodes.getJSONArray(seasonNum);
                            
                            for (int i = 0; i < seasonEpisodes.length(); i++) {
                                org.json.JSONObject episodeObj = seasonEpisodes.getJSONObject(i);
                                Episode episode = new Episode();
                                
                                episode.setId(episodeObj.optString("id"));
                                episode.setEpisode_num(episodeObj.optString("episode_num"));
                                episode.setTitle(episodeObj.optString("title"));
                                episode.setContainer_extension(episodeObj.optString("container_extension"));
                                episode.setMovie_image(episodeObj.optString("movie_image"));
                                episode.setPlot(episodeObj.optString("plot"));
                                episode.setDuration_secs(episodeObj.optString("duration_secs"));
                                episode.setDuration(episodeObj.optString("duration"));
                                episode.setVideo_quality(episodeObj.optString("video_quality"));
                                episode.setRelease_date(episodeObj.optString("release_date"));
                                episode.setRating(episodeObj.optString("rating"));
                                episode.setSeason_num(seasonNum);
                                episode.setSeries_id(seriesId);
                                
                                allEpisodes.add(episode);
                            }
                        }
                    }
                    
                    callback.onSuccess(allEpisodes);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing series info", e);
                    callback.onError("Erro ao processar informações da série: " + e.getMessage());
                }
            }
        });
    }
}