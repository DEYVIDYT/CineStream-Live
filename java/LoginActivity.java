package com.cinestream.live;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private MaterialButton goToRegisterButton;
    private ServerManager serverManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        goToRegisterButton = findViewById(R.id.register_button);
        
        serverManager = ServerManager.getInstance(this);
        
        // Load servers on startup
        loadServers();

        loginButton.setOnClickListener(v -> login());
        goToRegisterButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
    
    private void loadServers() {
        Log.d(TAG, "Loading servers...");
        
        serverManager.loadServers(new ServerManager.ServersLoadCallback() {
            @Override
            public void onSuccess(java.util.List<Server> servers) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Servers loaded successfully: " + servers.size() + " servers");
                    Toast.makeText(LoginActivity.this, "Servidores carregados: " + servers.size(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading servers: " + error);
                    Toast.makeText(LoginActivity.this, "Erro ao carregar servidores: " + error, Toast.LENGTH_LONG).show();
                });
            }
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
        
        Log.d(TAG, "Attempting login for email: " + email);
        
        // Disable login button during request
        loginButton.setEnabled(false);
        loginButton.setText("Entrando...");

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("device_id", deviceId)
                .build();

        serverManager.makeRequest("login.php", formBody, new ServerManager.ServerCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Log.d(TAG, "Login response: " + responseBody);
                
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Entrar");
                    
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            String sessionToken = jsonObject.getString("session_token");
                            int userId = jsonObject.getInt("user_id");
                            
                            // Salvar dados da sessão
                            SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("user_id", userId);
                            editor.putString("session_token", sessionToken);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HostActivity.class);

                            if (jsonObject.has("xtream_server")) {
                                String xtreamServer = jsonObject.getString("xtream_server");
                                String xtreamUsername = jsonObject.getString("xtream_username");
                                String xtreamPassword = jsonObject.getString("xtream_password");
                                intent.putExtra("xtream_server", xtreamServer);
                                intent.putExtra("xtream_username", xtreamUsername);
                                intent.putExtra("xtream_password", xtreamPassword);
                            }

                            startActivity(intent);
                            finish();
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
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing login response", e);
                        Toast.makeText(LoginActivity.this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Login error: " + error);
                
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Entrar");
                    Toast.makeText(LoginActivity.this, "Erro no login: " + error, Toast.LENGTH_LONG).show();
                });
            }
        }, this); // Added this (Activity context) as the 4th parameter
    }
}