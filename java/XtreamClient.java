package com.cinestream.live;

import android.util.Base64;
import android.util.Log;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

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
        // Create Gson with custom configuration for better error handling
        gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .registerTypeAdapter(String.class, new StringDeserializer())
                .create();
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
                    Log.d(TAG, "Raw JSON for series (length: " + jsonData.length() + "): " + jsonData.substring(0, Math.min(jsonData.length(), 500)) + "...");
                    
                    // Additional debugging for common issues
                    if (jsonData.length() == 0) {
                        Log.e(TAG, "Empty response from series API");
                        callback.onError("Resposta vazia da API de séries");
                        return;
                    }
                    
                    // Check for specific error responses (only check beginning of response)
                    String trimmedData = jsonData.trim();
                    if (trimmedData.startsWith("{\"error\"") || 
                        trimmedData.startsWith("{\"Error\"") ||
                        trimmedData.equals("null") ||
                        trimmedData.equals("false")) {
                        Log.e(TAG, "Error response from API: " + jsonData);
                        callback.onError("Erro na resposta da API: " + jsonData.substring(0, Math.min(jsonData.length(), 200)));
                        return;
                    }
                    
                    List<Series> series = parseSeriesJson(jsonData);
                    
                    if (series == null) {
                        series = new ArrayList<>();
                    }
                    
                    Log.d(TAG, "Successfully parsed " + series.size() + " series");
                    callback.onSuccess(series);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing series", e);
                    callback.onError("Erro ao processar séries: " + e.getMessage());
                }
            }
        });
    }
    
    private List<Series> parseSeriesJson(String jsonData) {
        try {
            // First, check if it's empty or null
            if (jsonData == null || jsonData.trim().isEmpty()) {
                Log.w(TAG, "Empty JSON data for series");
                return new ArrayList<>();
            }
            
            // Remove BOM if present
            if (jsonData.startsWith("\uFEFF")) {
                jsonData = jsonData.substring(1);
            }
            
            // Check if JSON starts with array bracket
            jsonData = jsonData.trim();
            if (!jsonData.startsWith("[")) {
                Log.w(TAG, "JSON does not start with array bracket. First 100 chars: " + 
                      jsonData.substring(0, Math.min(jsonData.length(), 100)));
                
                // Try to parse as object and extract array if possible
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
                    
                    // Some APIs return {"data": [...]} or similar
                    if (jsonObject.has("data")) {
                        org.json.JSONArray jsonArray = jsonObject.getJSONArray("data");
                        return parseSeriesArray(jsonArray);
                    } else if (jsonObject.has("series")) {
                        org.json.JSONArray jsonArray = jsonObject.getJSONArray("series");
                        return parseSeriesArray(jsonArray);
                    } else {
                        // If it's an object but not the expected structure, return empty list
                        Log.w(TAG, "JSON object does not contain expected array structure");
                        return new ArrayList<>();
                    }
                } catch (org.json.JSONException e) {
                    Log.e(TAG, "Failed to parse JSON as object", e);
                    return new ArrayList<>();
                }
            }
            
            // Try standard Gson parsing for array
            try {
                Type listType = new TypeToken<List<Series>>(){}.getType();
                List<Series> series = gson.fromJson(jsonData, listType);
                
                if (series == null) {
                    Log.w(TAG, "Gson returned null for series");
                    return new ArrayList<>();
                }
                
                // Validate and clean series data
                List<Series> validSeries = new ArrayList<>();
                for (Series s : series) {
                    if (s != null && s.getName() != null && !s.getName().trim().isEmpty()) {
                        validSeries.add(s);
                    } else {
                        Log.w(TAG, "Skipping invalid series: " + (s != null ? s.toString() : "null"));
                    }
                }
                
                Log.d(TAG, "Parsed " + validSeries.size() + " valid series out of " + series.size() + " total");
                return validSeries;
                
            } catch (JsonSyntaxException gsonError) {
                Log.w(TAG, "Gson parsing failed, trying manual parsing: " + gsonError.getMessage());
                
                // Fallback to manual parsing
                try {
                    org.json.JSONArray jsonArray = new org.json.JSONArray(jsonData);
                    return parseSeriesArray(jsonArray);
                } catch (org.json.JSONException jsonError) {
                    Log.e(TAG, "Both Gson and manual parsing failed", jsonError);
                    throw new RuntimeException("Não foi possível processar dados de séries: " + gsonError.getMessage());
                }
            }
            
        } catch (com.google.gson.JsonSyntaxException e) {
            Log.e(TAG, "JSON syntax error in series data", e);
            throw new RuntimeException("Formato JSON inválido para séries", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error parsing series JSON", e);
            throw new RuntimeException("Erro inesperado ao processar séries", e);
        }
    }
    
    private List<Series> parseSeriesArray(org.json.JSONArray jsonArray) {
        List<Series> series = new ArrayList<>();
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject seriesObj = jsonArray.getJSONObject(i);
                
                Series s = new Series();
                s.setNum(seriesObj.optString("num", ""));
                s.setName(seriesObj.optString("name", ""));
                s.setTitle(seriesObj.optString("title", ""));
                s.setYear(seriesObj.optString("year", ""));
                s.setSeries_id(seriesObj.optString("series_id", ""));
                s.setCover(seriesObj.optString("cover", ""));
                s.setPlot(seriesObj.optString("plot", ""));
                s.setCast(seriesObj.optString("cast", ""));
                s.setDirector(seriesObj.optString("director", ""));
                s.setGenre(seriesObj.optString("genre", ""));
                s.setRelease_date(seriesObj.optString("release_date", ""));
                s.setLast_modified(seriesObj.optString("last_modified", ""));
                s.setRating(seriesObj.optString("rating", ""));
                s.setRating_5based(seriesObj.optString("rating_5based", ""));
                s.setTmdb_id(seriesObj.optString("tmdb_id", ""));
                s.setCategory_id(seriesObj.optString("category_id", ""));
                s.setCategory_name(seriesObj.optString("category_name", ""));
                s.setBackdrop_path(seriesObj.optString("backdrop_path", ""));
                s.setYoutube_trailer(seriesObj.optString("youtube_trailer", ""));
                
                // Only add if has valid name
                if (s.getName() != null && !s.getName().trim().isEmpty()) {
                    series.add(s);
                }
            }
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error parsing series array manually", e);
        }
        
        return series;
    }
    
    // Custom deserializer to handle string conversion issues
    private static class StringDeserializer implements JsonDeserializer<String> {
        @Override
        public String deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return "";
            }
            
            if (json.isJsonPrimitive()) {
                // Handle different primitive types
                if (json.getAsJsonPrimitive().isNumber()) {
                    // Convert numbers to string
                    return json.getAsString();
                } else if (json.getAsJsonPrimitive().isBoolean()) {
                    // Convert boolean to string
                    return json.getAsString();
                } else {
                    // Already a string
                    return json.getAsString();
                }
            }
            
            // Handle arrays or objects by converting to string
            return json.toString();
        }
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