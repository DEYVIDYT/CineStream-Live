package com.example.cinestreamlive;

import android.util.Base64;
import com.example.cinestreamlive.model.Category;
import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.Credential;
import com.example.cinestreamlive.model.EpgEvent; // Import EpgEvent

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap; // For EPG data
import java.util.List;
import java.util.Map; // For EPG data
import java.util.Random;

public class XtreamCodesService {

    private static final String CREDENTIALS_URL = "https://raw.githubusercontent.com/DEYVIDYT/CineStream-Pro/refs/heads/main/credentials_base64.txt";
    private Credential selectedCredentialLocal;

    public XtreamCodesService() {
        // Construtor padrão
    }

    public Credential fetchAndSelectRandomCredential() throws IOException, JSONException {
        String base64Credentials = httpGet(CREDENTIALS_URL);
        if (base64Credentials == null || base64Credentials.isEmpty()) {
            throw new IOException("Failed to fetch credentials or credentials empty.");
        }

        byte[] decodedBytes = Base64.decode(base64Credentials, Base64.DEFAULT);
        String jsonCredentials = new String(decodedBytes);

        JSONArray credentialsArray = new JSONArray(jsonCredentials);
        if (credentialsArray.length() == 0) {
            throw new JSONException("No credentials found in JSON array.");
        }

        List<Credential> credentialsList = new ArrayList<>();
        for (int i = 0; i < credentialsArray.length(); i++) {
            credentialsList.add(Credential.fromJson(credentialsArray.getJSONObject(i)));
        }

        if (credentialsList.isEmpty()) {
            throw new JSONException("Parsed credentials list is empty.");
        }

        Random random = new Random();
        this.selectedCredentialLocal = credentialsList.get(random.nextInt(credentialsList.size()));
        return this.selectedCredentialLocal;
    }

    public List<Channel> fetchLiveStreams(Credential credential) throws IOException, JSONException {
        return fetchLiveStreamsForCategory(credential, null);
    }

    public List<Channel> fetchLiveStreamsForCategory(Credential credential, String categoryId) throws IOException, JSONException {
        if (credential == null) {
            throw new IOException("Credential cannot be null to fetch live streams.");
        }

        String actionUrl = (categoryId == null || categoryId.isEmpty() || "0".equals(categoryId)) ?
                           "&action=get_live_streams" :
                           "&action=get_live_streams&category_id=" + categoryId;

        String apiUrl = credential.getServer() + "/player_api.php?username=" + credential.getUsername() +
                        "&password=" + credential.getPassword() + actionUrl;

        String jsonResponse = httpGet(apiUrl);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Failed to fetch live streams or response empty. Category ID: " + categoryId);
        }

        if (jsonResponse.trim().startsWith("{")) {
            JSONObject errorCheck = new JSONObject(jsonResponse);
            if (errorCheck.has("user_info")) {
                JSONObject userInfo = errorCheck.getJSONObject("user_info");
                if (userInfo.has("auth") && userInfo.getInt("auth") == 0) {
                     throw new IOException("Authentication failed with Xtream Codes API. Message: " + userInfo.optString("message", "No message") + " Server: " + credential.getServer());
                }
            }
            throw new JSONException("Expected JSON array of channels, but got an object: " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
        }

        JSONArray channelsArray = new JSONArray(jsonResponse);
        List<Channel> channelList = new ArrayList<>();
        for (int i = 0; i < channelsArray.length(); i++) {
            JSONObject channelJson = channelsArray.getJSONObject(i);
            if ("live".equals(channelJson.optString("stream_type"))) {
                channelList.add(Channel.fromJson(channelJson));
            }
        }
        return channelList;
    }

    public List<Category> getLiveCategories(Credential credential) throws IOException, JSONException {
        if (credential == null) {
            throw new IOException("Credential cannot be null to fetch categories.");
        }
        String apiUrl = credential.getServer() + "/player_api.php?username=" + credential.getUsername() +
                        "&password=" + credential.getPassword() + "&action=get_live_categories";

        String jsonResponse = httpGet(apiUrl);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Failed to fetch categories or response empty.");
        }

        if (jsonResponse.trim().startsWith("{")) {
            JSONObject errorCheck = new JSONObject(jsonResponse);
            if (errorCheck.has("user_info")) {
                JSONObject userInfo = errorCheck.getJSONObject("user_info");
                if (userInfo.has("auth") && userInfo.getInt("auth") == 0) {
                     throw new IOException("Authentication failed when fetching categories. Message: " + userInfo.optString("message", "No message"));
                }
            }
            throw new JSONException("Expected JSON array of categories, but got an object: " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 100)));
        }

        JSONArray categoriesArray = new JSONArray(jsonResponse);
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("0", "Todos os Canais", "-1"));

        for (int i = 0; i < categoriesArray.length(); i++) {
            categoryList.add(Category.fromJson(categoriesArray.getJSONObject(i)));
        }
        return categoryList;
    }

    public Map<String, List<EpgEvent>> fetchEpgDataForCategory(Credential credential, String categoryId) throws IOException, JSONException {
        if (credential == null) {
            throw new IOException("Credential cannot be null to fetch EPG data.");
        }
        // Se categoryId for "0" (Todos os Canais), não passamos category_id para pegar EPG de todos os canais ao vivo.
        // A API pode ter um limite, então buscar EPG para "Todos" pode ser pesado ou não suportado.
        // Por enquanto, vamos assumir que se categoryId é "0" ou null, pegamos para todos os canais live.
        // A API pode usar "type=live" para isso, ou simplesmente não filtrar por categoria.
        String categoryParam = (categoryId == null || "0".equals(categoryId) || categoryId.isEmpty()) ? "" : "&category_id=" + categoryId;
        // Testar se "&type=live" é necessário ou se a ausência de category_id já faz isso.
        // Por segurança, podemos adicionar type=live se não houver category_id.
        if (categoryParam.isEmpty()) {
            categoryParam = "&type=live"; // Ou ajuste conforme a API específica
        }

        String apiUrl = credential.getServer() + "/player_api.php?username=" + credential.getUsername() +
                        "&password=" + credential.getPassword() + "&action=get_simple_data_table" + categoryParam;

        String jsonResponse = httpGet(apiUrl);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Failed to fetch EPG data or response empty. Category ID: " + categoryId);
        }

        JSONObject responseObject = new JSONObject(jsonResponse);
        JSONArray epgListingsArray = responseObject.optJSONArray("epg_listings");

        Map<String, List<EpgEvent>> epgDataMap = new HashMap<>();
        if (epgListingsArray != null) {
            for (int i = 0; i < epgListingsArray.length(); i++) {
                JSONObject eventJson = epgListingsArray.getJSONObject(i);
                EpgEvent event = EpgEvent.fromJson(eventJson);
                String chId = event.getChannelId();
                if (!epgDataMap.containsKey(chId)) {
                    epgDataMap.put(chId, new ArrayList<>());
                }
                epgDataMap.get(chId).add(event);
            }
        }
        return epgDataMap;
    }

    public String getChannelStreamUrl(Credential credential, int streamId) {
        if (credential == null) return null;
        return credential.getServer() + "/live/" + credential.getUsername() + "/" + credential.getPassword() + "/" + streamId + ".m3u8";
    }

    public String getChannelStreamUrl(Credential credential, Channel channel) {
        if (credential == null || channel == null) return null;
        return getChannelStreamUrl(credential, channel.getStreamId());
    }

    public Credential getSelectedCredential() {
        return selectedCredentialLocal;
    }

    private String httpGet(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                InputStream errorStream = urlConnection.getErrorStream();
                String errorBody = "No error body";
                if (errorStream != null) {
                    StringBuilder errorBuffer = new StringBuilder();
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorBuffer.append(errorLine);
                    }
                    errorBody = errorBuffer.toString();
                    errorReader.close();
                }
                throw new IOException("HTTP error code: " + responseCode + " for URL: " + urlString + ". Body: " + errorBody);
            }

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            result = buffer.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Log e
                }
            }
        }
        return result;
    }
}
