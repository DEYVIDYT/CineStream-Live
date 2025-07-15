package com.cinestream.live;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.videolan.libvlc.util.VLCVideoLayout;

public class EpisodePlayerActivity extends AppCompatActivity implements VlcVideoPlayer.PlayerControlsListener {

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
    private ImageButton playPauseButton;
    private ImageButton rewindButton;
    private ImageButton forwardButton;
    private LinearLayout videoControlsContainer;
    private SeekBar progressSeekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    
    private boolean isFullscreen = false;
    private boolean controlsVisible = true;
    private boolean userSeeking = false;
    
    private Handler hideControlsHandler;
    private Runnable hideControlsRunnable;
    private GestureDetector gestureDetector;
    
    private static final int HIDE_CONTROLS_DELAY = 3000; // 3 segundos

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
        setupControlsHandling();
        setupEpisodeInfo();
        setupListeners();
        startPlayer();
    }

    private void initViews() {
        vlcVideoLayout = findViewById(R.id.vlcVideoLayout);
        seriesTitleTextView = findViewById(R.id.seriesTitleTextView);
        episodeInfoTextView = findViewById(R.id.episodeInfoTextView);
        plotTextView = findViewById(R.id.plotTextView);
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        rewindButton = findViewById(R.id.rewindButton);
        forwardButton = findViewById(R.id.forwardButton);
        videoControlsContainer = findViewById(R.id.videoControlsContainer);
        progressSeekBar = findViewById(R.id.progressSeekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
    }
    
    private void setupControlsHandling() {
        hideControlsHandler = new Handler(Looper.getMainLooper());
        hideControlsRunnable = this::hideControls;
        
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControlsVisibility();
                return true;
            }
        });
        
        vlcVideoLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
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
        vlcVideoPlayer.setControlsListener(this);
        vlcVideoPlayer.play(streamUrl);
        startHideControlsTimer();
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        rewindButton.setOnClickListener(v -> seekBackward());
        forwardButton.setOnClickListener(v -> seekForward());
        
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && vlcVideoPlayer != null) {
                    userSeeking = true;
                    long duration = vlcVideoPlayer.getLength();
                    if (duration > 0) {
                        long newTime = (duration * progress) / 100;
                        currentTimeTextView.setText(VlcVideoPlayer.formatTime(newTime));
                    }
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
                pauseHideControlsTimer();
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (vlcVideoPlayer != null) {
                    long duration = vlcVideoPlayer.getLength();
                    if (duration > 0) {
                        float position = (float) seekBar.getProgress() / 100;
                        vlcVideoPlayer.setPosition(position);
                    }
                }
                userSeeking = false;
                startHideControlsTimer();
            }
        });
    }
    
    // Implementação da interface PlayerControlsListener
    @Override
    public void onPositionChanged(float position) {
        if (!userSeeking) {
            runOnUiThread(() -> {
                int progress = (int) (position * 100);
                progressSeekBar.setProgress(progress);
            });
        }
    }
    
    @Override
    public void onTimeChanged(long time) {
        if (!userSeeking) {
            runOnUiThread(() -> {
                currentTimeTextView.setText(VlcVideoPlayer.formatTime(time));
            });
        }
    }
    
    @Override
    public void onLengthChanged(long length) {
        runOnUiThread(() -> {
            totalTimeTextView.setText(VlcVideoPlayer.formatTime(length));
        });
    }
    
    @Override
    public void onPlayingStateChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play_arrow);
            }
        });
    }
    
    // Métodos de controle
    private void togglePlayPause() {
        if (vlcVideoPlayer != null) {
            if (vlcVideoPlayer.isPlaying()) {
                vlcVideoPlayer.pause();
            } else {
                vlcVideoPlayer.resume();
            }
        }
        startHideControlsTimer();
    }
    
    private void seekForward() {
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.seekForward(10000); // 10 segundos
        }
        startHideControlsTimer();
    }
    
    private void seekBackward() {
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.seekBackward(10000); // 10 segundos
        }
        startHideControlsTimer();
    }
    
    // Controle de visibilidade dos controles
    private void toggleControlsVisibility() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }
    
    private void showControls() {
        controlsVisible = true;
        videoControlsContainer.setVisibility(View.VISIBLE);
        startHideControlsTimer();
    }
    
    private void hideControls() {
        controlsVisible = false;
        videoControlsContainer.setVisibility(View.GONE);
        pauseHideControlsTimer();
    }
    
    private void startHideControlsTimer() {
        pauseHideControlsTimer();
        if (controlsVisible && vlcVideoPlayer != null && vlcVideoPlayer.isPlaying()) {
            hideControlsHandler.postDelayed(hideControlsRunnable, HIDE_CONTROLS_DELAY);
        }
    }
    
    private void pauseHideControlsTimer() {
        hideControlsHandler.removeCallbacks(hideControlsRunnable);
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
        controlsVisible = false;
        pauseHideControlsTimer();
        videoControlsContainer.setVisibility(View.GONE);
        
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
        showControls();
        
        // Show system UI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseHideControlsTimer();
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseHideControlsTimer();
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.release();
        }
    }
}