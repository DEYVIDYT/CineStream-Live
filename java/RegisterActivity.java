package com.cinestream.live;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button register;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progress_bar);

        register.setOnClickListener(v -> {
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            progressBar.setVisibility(View.VISIBLE);

            ApiClient apiClient = new ApiClient();
            apiClient.register(emailStr, passwordStr, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    if (response != null && response.contains("\"success\":true")) {
                        showToast("Registration successful");
                        finish(); // Go back to LoginActivity
                    } else {
                        String errorMessage = "Registration failed";
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
        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
