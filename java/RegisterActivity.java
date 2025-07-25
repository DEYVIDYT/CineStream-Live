package com.vplay.live;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
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

public class RegisterActivity extends AppCompatActivity {
    
    private static final String TAG = "RegisterActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private MaterialButton goToLoginButton;
    private ServerManager serverManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        termsCheckBox = findViewById(R.id.terms_checkbox);
        registerButton = findViewById(R.id.register_button);
        goToLoginButton = findViewById(R.id.login_button);
        
        serverManager = ServerManager.getInstance(this);
        
        // Load servers if not already loaded
        loadServers();

        registerButton.setOnClickListener(v -> register());
        goToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
    
    private void loadServers() {
        Log.d(TAG, "Loading servers...");
        
        serverManager.loadServers(new ServerManager.ServersLoadCallback() {
            @Override
            public void onSuccess(java.util.List<Server> servers) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Servers loaded successfully: " + servers.size() + " servers");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading servers: " + error);
                    Toast.makeText(RegisterActivity.this, "Erro ao carregar servidores: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void register() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "Você deve concordar com os termos de uso.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Attempting registration for email: " + email);
        
        // Disable register button during request
        registerButton.setEnabled(false);
        registerButton.setText("Registrando...");

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("device_id", deviceId)
                .build();

        serverManager.makeRequest("register.php", formBody, new ServerManager.ServerCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Log.d(TAG, "Register response: " + responseBody);
                
                runOnUiThread(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Registrar");
                    
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            Toast.makeText(RegisterActivity.this, "Registro bem-sucedido! Faça login para continuar.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String message = jsonObject.optString("message", "Erro desconhecido.");
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing register response", e);
                        Toast.makeText(RegisterActivity.this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Register error: " + error);
                
                runOnUiThread(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Registrar");
                    Toast.makeText(RegisterActivity.this, "Erro no registro: " + error, Toast.LENGTH_LONG).show();
                });
            }
        }, this); // Added this (Activity context) as the 4th parameter
    }
}