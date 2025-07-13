package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        sharedViewModel.loadData();

        sharedViewModel.getIsLoading().observe(this, isLoading -> {
            if (!isLoading) {
                startActivity(new Intent(MainActivity.this, HostActivity.class));
                finish();
            }
        });
    }
}