package com.vplay.live;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final String UPDATE_URL = "https://raw.githubusercontent.com/DEYVIDYT/Server_vplay/refs/heads/main/Update.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForUpdate();
    }

    private void checkForUpdate() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(UPDATE_URL).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                // Se a verificação de atualização falhar, prossiga com o fluxo normal
                proceedWithAppFlow();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String latestVersion = jsonObject.getString("versao");
                        String updateUrl = jsonObject.getString("url");

                        if (isUpdateRequired(latestVersion)) {
                            showUpdateDialog(updateUrl);
                        } else {
                            proceedWithAppFlow();
                        }
                    } catch (JSONException e) {
                        proceedWithAppFlow();
                    }
                } else {
                    proceedWithAppFlow();
                }
            }
        });
    }

    private boolean isUpdateRequired(String latestVersion) {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String currentVersion = pInfo.versionName;
            return !currentVersion.equals(latestVersion);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showUpdateDialog(String updateUrl) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Atualização Disponível")
                .setMessage("Uma nova versão do aplicativo está disponível. Por favor, atualize para continuar.")
                .setCancelable(false)
                .setPositiveButton("Atualizar", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                    startActivity(browserIntent);
                    finish();
                }).show());
    }

    private void proceedWithAppFlow() {
        // Simple splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("VplayPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            String sessionToken = prefs.getString("session_token", null);

            if (userId != -1 && sessionToken != null) {
                refreshCredentialsAndStart(userId, sessionToken);
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000); // 2 second delay
    }

    private void refreshCredentialsAndStart(int userId, String sessionToken) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("session_token", sessionToken)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/refresh_xtream_credentials.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                // Se falhar, ir para a tela de login
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body().string();
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    if (status.equals("success")) {
                        String xtreamServer = jsonObject.getString("xtream_server");
                        String xtreamUsername = jsonObject.getString("xtream_username");
                        String xtreamPassword = jsonObject.getString("xtream_password");

                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        intent.putExtra("xtream_server", xtreamServer);
                        intent.putExtra("xtream_username", xtreamUsername);
                        intent.putExtra("xtream_password", xtreamPassword);
                        startActivity(intent);
                        finish();
                    } else if (status.equals("plan_expired")) {
                        // Plano expirado - redirecionar para tela de renovação
                        startActivity(new Intent(MainActivity.this, PlanExpirationActivity.class));
                        finish();
                    } else {
                        // Se a sessão for inválida, ir para a tela de login
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                } catch (org.json.JSONException e) {
                    // Se houver erro no JSON, ir para a tela de login
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }
}