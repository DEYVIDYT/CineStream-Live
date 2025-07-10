package com.example.cinestreamlive;

import android.util.Base64;
import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.Credential;
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
import java.util.List;
import java.util.Random;

public class XtreamCodesService {

    private static final String CREDENTIALS_URL = "https://raw.githubusercontent.com/DEYVIDYT/CineStream-Pro/refs/heads/main/credentials_base64.txt";
    private Credential selectedCredential; // Armazenar a credencial selecionada

    // Método para buscar, decodificar e selecionar credencial (síncrono)
    // Retorna a credencial para que possa ser usada para construir a URL do stream mais tarde.
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
        selectedCredential = credentialsList.get(random.nextInt(credentialsList.size()));
        return selectedCredential;
    }

    // Método para buscar canais (síncrono)
    public List<Channel> fetchLiveStreams(Credential credential) throws IOException, JSONException {
        if (credential == null) {
            throw new IOException("Credential cannot be null to fetch live streams.");
        }

        String apiUrl = credential.getServer() + "/player_api.php?username=" + credential.getUsername() +
                        "&password=" + credential.getPassword() + "&action=get_live_streams";

        String jsonResponse = httpGet(apiUrl);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Failed to fetch live streams or response empty.");
        }

        if (jsonResponse.trim().startsWith("{")) {
            JSONObject errorCheck = new JSONObject(jsonResponse);
            if (errorCheck.has("user_info")) {
                JSONObject userInfo = errorCheck.getJSONObject("user_info");
                if (userInfo.has("auth") && userInfo.getInt("auth") == 0) { // Auth failed
                     throw new IOException("Authentication failed with Xtream Codes API. Message: " + userInfo.optString("message", "No message") + " Server: " + credential.getServer());
                }
            }
            // Se não for erro de auth mas ainda for um objeto, pode ser outro tipo de erro ou formato inesperado
            throw new JSONException("Expected JSON array of channels, but got an object: " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
        }

        JSONArray channelsArray = new JSONArray(jsonResponse);
        List<Channel> channelList = new ArrayList<>();
        for (int i = 0; i < channelsArray.length(); i++) {
            // Adicionar apenas streams do tipo "live"
            JSONObject channelJson = channelsArray.getJSONObject(i);
            if ("live".equals(channelJson.optString("stream_type"))) {
                channelList.add(Channel.fromJson(channelJson));
            }
        }
        return channelList;
    }

    // Método para construir a URL de streaming de um canal
    // Usa a `selectedCredential` que foi armazenada
    public String getChannelStreamUrl(int streamId) {
        if (selectedCredential == null) return null;
        return selectedCredential.getServer() + "/live/" + selectedCredential.getUsername() + "/" + selectedCredential.getPassword() + "/" + streamId + ".m3u8";
    }

    public String getChannelStreamUrl(Channel channel) {
        if (selectedCredential == null || channel == null) return null;
        return getChannelStreamUrl(channel.getStreamId());
    }

    // Getter para a credencial selecionada, caso a MainActivity precise dela.
    public Credential getSelectedCredential() {
        return selectedCredential;
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
                // Ler a resposta de erro, se houver
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
