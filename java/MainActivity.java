package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private XtreamClient xtreamClient;
    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        xtreamClient = new XtreamClient();
        handler = new Handler(Looper.getMainLooper());
        
        loadCredentials();
    }
    
    private void loadCredentials() {
        xtreamClient.fetchCredentials(new XtreamClient.CredentialsCallback() {
            @Override
            public void onSuccess(Credential credential) {
                handler.post(() -> {
                    handler.postDelayed(() -> {
                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        startActivity(intent);
                        finish();
                    }, 2000);
                });
            }
            
            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    
                    handler.postDelayed(() -> {
                        loadCredentials();
                    }, 3000);
                });
            }
        });
    }
}