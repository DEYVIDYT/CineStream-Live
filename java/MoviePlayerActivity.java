package com.cinestream.live;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.videolan.libvlc.util.VLCVideoLayout;

public class MoviePlayerActivity extends AppCompatActivity {

    private VLCVideoLayout vlcVideoLayout;
    private VlcVideoPlayer vlcVideoPlayer;
    private Movie movie;
    private String streamUrl;
    private TextView titleTextView;
    private ImageButton backButton;
    private ImageButton fullscreenButton;
    private ProgressBar videoLoadingProgressBar;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_player);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");
        streamUrl = intent.getStringExtra("stream_url");

        if (movie == null || streamUrl == null) {
            Toast.makeText(this, "Erro: Dados do filme não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        startPlayer();
    }

    private void initViews() {
        vlcVideoLayout = findViewById(R.id.vlcVideoLayout);
        titleTextView = findViewById(R.id.titleTextView);
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        videoLoadingProgressBar = findViewById(R.id.videoLoadingProgressBar);

        if (movie.getName() != null) {
            titleTextView.setText(movie.getName());
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void startPlayer() {
        videoLoadingProgressBar.setVisibility(View.VISIBLE);
        
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.stop();
        }
        
        vlcVideoPlayer = new VlcVideoPlayer(this, vlcVideoLayout);
        vlcVideoPlayer.play(streamUrl);
        
        videoLoadingProgressBar.setVisibility(View.GONE);
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        isFullscreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Hide UI elements
        findViewById(R.id.playerControlsContainer).setVisibility(View.GONE);
        
        // Hide system UI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void exitFullscreen() {
        isFullscreen = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Show UI elements
        findViewById(R.id.playerControlsContainer).setVisibility(View.VISIBLE);
        
        // Show system UI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.release();
        }
    }
}