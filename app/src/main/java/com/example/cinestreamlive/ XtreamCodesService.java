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

    public interface ServiceCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // Método para buscar, decodificar e selecionar credencial (síncrono, para ser chamado em background thread)
    private Credential fetchAndSelectRandomCredential() throws IOException, JSONException {
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
        return credentialsList.get(random.nextInt(credentialsList.size()));
    }

    // Método para buscar canais (síncrono)
    public List<Channel> fetchLiveStreams() throws IOException, JSONException {
        Credential credential = fetchAndSelectRandomCredential();
        if (credential == null) {
            throw new IOException("No valid credential selected.");
        }

        String apiUrl = credential.getServer() + "/player_api.php?username=" + credential.getUsername() +
                        "&password=" + credential.getPassword() + "&action=get_live_streams";

        String jsonResponse = httpGet(apiUrl);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IOException("Failed to fetch live streams or response empty.");
        }

        // A API XtreamCodes pode retornar um JSON Object se for um erro de autenticação
        // ou um JSON Array se for sucesso.
        // {"user_info":{"auth":0,...}} <- falha
        // [...] <- sucesso
        if (jsonResponse.trim().startsWith("{")) {
            JSONObject errorCheck = new JSONObject(jsonResponse);
            if (errorCheck.has("user_info")) {
                JSONObject userInfo = errorCheck.getJSONObject("user_info");
                if (userInfo.has("auth") && userInfo.getInt("auth") == 0) {
                     throw new IOException("Authentication failed with Xtream Codes API. Message: " + userInfo.optString("message", "No message"));
                }
            }
             // Se não for erro de auth mas ainda for um objeto, pode ser outro tipo de erro ou formato inesperado
            throw new JSONException("Expected JSON array of channels, but got an object: " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 100)));
        }


        JSONArray channelsArray = new JSONArray(jsonResponse);
        List<Channel> channelList = new ArrayList<>();
        for (int i = 0; i < channelsArray.length(); i++) {
            channelList.add(Channel.fromJson(channelsArray.getJSONObject(i)));
        }
        return channelList;
    }

    // Método para construir a URL de streaming de um canal
    public String getChannelStreamUrl(Credential credential, int streamId) {
        if (credential == null) return null;
        // Formato comum: http://<server>:<port>/live/<username>/<password>/<stream_id>.m3u8
        // Algumas APIs podem não precisar da porta se for a padrão (80)
        // Ou o formato pode ser /<username>/<password>/<stream_id> se for para TV ao vivo
        return credential.getServer() + "/live/" + credential.getUsername() + "/" + credential.getPassword() + "/" + streamId + ".m3u8";
    }

    // Sobrecarga para conveniência se já tivermos o objeto Credential e Channel
    public String getChannelStreamUrl(Credential credential, Channel channel) {
        if (credential == null || channel == null) return null;
        return getChannelStreamUrl(credential, channel.getStreamId());
    }


    private String httpGet(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000); // 15 segundos
            urlConnection.setReadTimeout(15000);    // 15 segundos
            urlConnection.connect();

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
