package com.cinestream.live;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.videolan.libvlc.util.VLCVideoLayout;

public class EpisodePlayerActivity extends AppCompatActivity {

    private VLCVideoLayout vlcVideoLayout;
    private VlcVideoPlayer vlcVideoPlayer;
    private Episode episode;
    private Series series;
    private String streamUrl;
    private TextView seriesTitleTextView;
    private TextView episodeInfoTextView;
    private TextView plotTextView;
    private ImageButton backButton;
    private ImageButton fullscreenButton;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_player);

        Intent intent = getIntent();
        episode = intent.getParcelableExtra("episode");
        series = intent.getParcelableExtra("series");
        streamUrl = intent.getStringExtra("stream_url");

        if (episode == null || series == null || streamUrl == null) {
            Toast.makeText(this, "Erro: Dados do episódio não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupEpisodeInfo();
        startPlayer();
    }

    private void initViews() {
        vlcVideoLayout = findViewById(R.id.vlcVideoLayout);
        seriesTitleTextView = findViewById(R.id.seriesTitleTextView);
        episodeInfoTextView = findViewById(R.id.episodeInfoTextView);
        plotTextView = findViewById(R.id.plotTextView);
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);

        backButton.setOnClickListener(v -> finish());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void setupEpisodeInfo() {
        if (series.getName() != null) {
            seriesTitleTextView.setText(series.getName());
        }

        String episodeInfo = episode.getFormattedEpisodeNumber();
        if (episode.getTitle() != null && !episode.getTitle().isEmpty()) {
            episodeInfo += " - " + episode.getTitle();
        }
        episodeInfoTextView.setText(episodeInfo);

        if (episode.getPlot() != null && !episode.getPlot().isEmpty()) {
            plotTextView.setText(episode.getPlot());
            plotTextView.setVisibility(View.VISIBLE);
        } else {
            plotTextView.setVisibility(View.GONE);
        }
    }

    private void startPlayer() {
        vlcVideoPlayer = new VlcVideoPlayer(this, vlcVideoLayout);
        vlcVideoPlayer.play(streamUrl);
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
        findViewById(R.id.episodeInfoContainer).setVisibility(View.GONE);
        
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
        findViewById(R.id.episodeInfoContainer).setVisibility(View.VISIBLE);
        
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