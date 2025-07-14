package com.cinestream.live;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerTextView = findViewById(R.id.register_text);

        loginButton.setOnClickListener(v -> login());
        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("device_id", deviceId)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/login.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erro de rede.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    runOnUiThread(() -> {
                        if (status.equals("success")) {
                            try {
                                String sessionToken = jsonObject.getString("session_token");
                                int userId = jsonObject.getInt("user_id");
                                String xtreamServer = jsonObject.getString("xtream_server");
                                String xtreamUsername = jsonObject.getString("xtream_username");
                                String xtreamPassword = jsonObject.getString("xtream_password");

                                // Salvar dados da sessão
                                SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("user_id", userId);
                                editor.putString("session_token", sessionToken);
                                editor.putString("xtream_server", xtreamServer);
                                editor.putString("xtream_username", xtreamUsername);
                                editor.putString("xtream_password", xtreamPassword);
                                editor.apply();

                                Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, HostActivity.class));
                                finish();
                            } catch (JSONException e) {
                                Toast.makeText(LoginActivity.this, "Erro ao processar os dados do servidor.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (status.equals("banned")) {
                            String message = jsonObject.optString("message", "Este usuário ou dispositivo foi banido.");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            // Fechar o app após um pequeno atraso para o usuário ler a mensagem
                            new android.os.Handler().postDelayed(
                                () -> finishAffinity(),
                                3000
                            );
                        } else {
                            String message = jsonObject.optString("message", "Erro desconhecido.");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
