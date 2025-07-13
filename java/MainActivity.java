package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private XtreamClient xtreamClient;
    private Handler handler;
    private List<Channel> allChannels;
    private List<Category> allCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xtreamClient = new XtreamClient();
        handler = new Handler(Looper.getMainLooper());

        loadInitialData();
    }

    private void loadInitialData() {
        xtreamClient.fetchCredentials(new XtreamClient.CredentialsCallback() {
            @Override
            public void onSuccess(Credential credential) {
                // Credentials fetched, now fetch channels and categories
                fetchCategoriesAndChannels();
            }

            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    // Retry after a delay
                    handler.postDelayed(() -> loadInitialData(), 3000);
                });
            }
        });
    }

    private void fetchCategoriesAndChannels() {
        xtreamClient.fetchLiveCategories(new XtreamClient.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                allCategories = categories;
                // Now fetch channels
                xtreamClient.fetchLiveStreams(new XtreamClient.ChannelsCallback() {
                    @Override
                    public void onSuccess(List<Channel> channels) {
                        allChannels = channels;
                        // All data loaded, start HostActivity
                        startHostActivity();
                    }

                    @Override
                    public void onError(String error) {
                        handler.post(() -> {
                            Toast.makeText(MainActivity.this, "Erro ao carregar canais: " + error, Toast.LENGTH_LONG).show();
                            // Retry fetching categories and channels
                            handler.postDelayed(() -> fetchCategoriesAndChannels(), 3000);
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "Erro ao carregar categorias: " + error, Toast.LENGTH_LONG).show();
                    // Retry fetching categories and channels
                    handler.postDelayed(() -> fetchCategoriesAndChannels(), 3000);
                });
            }
        });
    }

    private void startHostActivity() {
        Intent intent = new Intent(MainActivity.this, HostActivity.class);
        intent.putParcelableArrayListExtra("channels", (ArrayList<Channel>) allChannels);
        intent.putParcelableArrayListExtra("categories", (ArrayList<Category>) allCategories);
        startActivity(intent);
        finish();
    }
}