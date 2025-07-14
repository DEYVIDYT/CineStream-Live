package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView register;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progress_bar);

        login.setOnClickListener(v -> {
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            progressBar.setVisibility(View.VISIBLE);

            ApiClient apiClient = new ApiClient();
            apiClient.login(emailStr, passwordStr, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    if (response != null && response.contains("\"success\":true")) {
                        String token = response.split("\"token\":\"")[1].split("\"")[0];
                        saveSessionToken(token);

                        Intent intent = new Intent(LoginActivity.this, HostActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Invalid credentials";
                        if (response != null && response.contains("message")) {
                            errorMessage = response.split("\"message\":\"")[1].split("\"")[0];
                        }
                        showToast(errorMessage);
                    }
                }

                @Override
                public void onFailure(String message) {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    showToast(message);
                }
            });
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void saveSessionToken(String token) {
        android.content.SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        editor.putString("session_token", token);
        editor.apply();

        register.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
