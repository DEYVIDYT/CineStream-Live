package com.cinestream.live;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);

        register.setOnClickListener(v -> {
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            ApiClient apiClient = new ApiClient();
            apiClient.register(emailStr, passwordStr, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    if (response != null && response.contains("\"success\":true")) {
                        runOnUiThread(() -> {
                            // Show success message and navigate to login
                            finish(); // Go back to LoginActivity
                        });
                    } else {
                        runOnUiThread(() -> {
                            // Show error message
                        });
                    }
                }

                @Override
                public void onFailure(String message) {
                    // Show error message
                }
            });
        });
    }
}
