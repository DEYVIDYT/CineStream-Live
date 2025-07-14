package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Simple splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Verificar se o usuário já está logado
            SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
            if (prefs.contains("user_id") && prefs.contains("session_token")) {
                startActivity(new Intent(MainActivity.this, HostActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000); // 2 second delay
    }
}