package com.cinestream.live;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ApiClient {

    private static final String BASE_URL = "http://mybrasiltv.x10.mx/";
    private OkHttpClient client = new OkHttpClient();

    public void login(String email, String password, final ApiCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "server/login.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Request failed");
                }
            }
        });
    }

    public void register(String email, String password, final ApiCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "server/register.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Request failed");
                }
            }
        });
    }

    public void getUserData(String token, final ApiCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "server/get_user_data.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Request failed");
                }
            }
        });
    }

    public void checkSession(String token, final ApiCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "server/check_session.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Request failed");
                }
            }
        });
    }

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String message);
    }
}
