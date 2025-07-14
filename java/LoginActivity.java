package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        login.setOnClickListener(v -> {
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            ApiClient apiClient = new ApiClient();
            apiClient.login(emailStr, passwordStr, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            String token = json.getString("token");
                            saveSessionToken(token);

                            Intent intent = new Intent(LoginActivity.this, HostActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            runOnUiThread(() -> {
                                // Show error message
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String message) {
                    // Show error message
                }
            });
        });
    }

    private void saveSessionToken(String token) {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("session_token", token);
        editor.apply();

        register.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
