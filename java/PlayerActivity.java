package com.cinestream.live;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.videolan.libvlc.util.VLCVideoLayout;

public class PlayerActivity extends AppCompatActivity implements VlcVideoPlayer.PlayerControlsListener {

    private VLCVideoLayout vlcVideoLayout;
    private VlcVideoPlayer vlcVideoPlayer;
    private Channel channel;
    private String streamUrl;
    private TextView titleTextView;
    private TextView channelInfoTextView;
    private TextView liveIndicator;
    private ImageButton aspectRatioButton;
    private ImageButton pipButton;
    private ImageButton backButton;
    private ImageButton fullscreenButton;
    private ImageButton playPauseButton;
    private ProgressBar videoLoadingProgressBar;
    private LinearLayout playerControlsContainer;
    private LinearLayout bottomInfoContainer;
    
    private int currentAspectRatio = 0; // 0: Original, 1: 16:9, 2: 4:3, 3: Stretch
    private String[] aspectRatioNames = {"Original", "16:9", "4:3", "Esticado"};
    private boolean isInPictureInPictureMode = false;
    private boolean isFullscreen = false;
    private boolean controlsVisible = true;
    
    private Handler hideControlsHandler;
    private Runnable hideControlsRunnable;
    private GestureDetector gestureDetector;
    
    private static final int HIDE_CONTROLS_DELAY = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        channel = intent.getParcelableExtra("channel");
        streamUrl = intent.getStringExtra("stream_url");

        if (channel == null || streamUrl == null) {
            Toast.makeText(this, "Erro: Dados do canal não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupControlsHandling();
        setupListeners();
        startPlayer();
    }
    
    private void initViews() {
        vlcVideoLayout = findViewById(R.id.vlcVideoLayout);
        titleTextView = findViewById(R.id.titleTextView);
        channelInfoTextView = findViewById(R.id.channelInfoTextView);
        liveIndicator = findViewById(R.id.liveIndicator);
        aspectRatioButton = findViewById(R.id.aspectRatioButton);
        pipButton = findViewById(R.id.pipButton);
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        videoLoadingProgressBar = findViewById(R.id.videoLoadingProgressBar);
        playerControlsContainer = findViewById(R.id.playerControlsContainer);
        bottomInfoContainer = findViewById(R.id.bottomInfoContainer);

        if (channel.getName() != null) {
            titleTextView.setText(channel.getName());
        }
        
        // Mostrar informações do canal (se for transmissão ao vivo)
        if (streamUrl.contains("live") || streamUrl.contains("tv")) {
            bottomInfoContainer.setVisibility(View.VISIBLE);
            liveIndicator.setVisibility(View.VISIBLE);
            if (channelInfoTextView != null) {
                channelInfoTextView.setText("Transmissão ao vivo");
            }
        }
        
        // Verificar se o dispositivo suporta PIP
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !getPackageManager().hasSystemFeature(
                "android.software.picture_in_picture")) {
            pipButton.setVisibility(View.GONE);
        }
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

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        aspectRatioButton.setOnClickListener(v -> toggleAspectRatio());
        pipButton.setOnClickListener(v -> startPictureInPictureMode());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
    }

    private void startPlayer() {
        videoLoadingProgressBar.setVisibility(View.VISIBLE);
        
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.stop();
        }
        
        vlcVideoPlayer = new VlcVideoPlayer(this, vlcVideoLayout);
        vlcVideoPlayer.setControlsListener(this);
        vlcVideoPlayer.play(streamUrl);
        
        videoLoadingProgressBar.setVisibility(View.GONE);
        startHideControlsTimer();
    }
    
    // Implementação da interface PlayerControlsListener
    @Override
    public void onPositionChanged(float position) {
        // Não aplicável para streams ao vivo
    }
    
    @Override
    public void onTimeChanged(long time) {
        // Não aplicável para streams ao vivo
    }
    
    @Override
    public void onLengthChanged(long length) {
        // Não aplicável para streams ao vivo
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
        
        controlsVisible = false;
        pauseHideControlsTimer();
        playerControlsContainer.setVisibility(View.GONE);
        
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
        showControls();
        
        // Show system UI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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
        playerControlsContainer.setVisibility(View.VISIBLE);
        startHideControlsTimer();
    }
    
    private void hideControls() {
        controlsVisible = false;
        playerControlsContainer.setVisibility(View.GONE);
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
    
    // Métodos para aspect ratio
    private void toggleAspectRatio() {
        currentAspectRatio = (currentAspectRatio + 1) % aspectRatioNames.length;
        applyAspectRatio();
        Toast.makeText(this, "Proporção: " + aspectRatioNames[currentAspectRatio], Toast.LENGTH_SHORT).show();
        startHideControlsTimer();
    }
    
    private void applyAspectRatio() {
        if (vlcVideoLayout != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) vlcVideoLayout.getLayoutParams();
            
            switch (currentAspectRatio) {
                case 0: // Original
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    vlcVideoLayout.setLayoutParams(params);
                    break;
                case 1: // 16:9
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    params.height = (int) (params.width * 9.0 / 16.0);
                    vlcVideoLayout.setLayoutParams(params);
                    break;
                case 2: // 4:3
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    params.height = (int) (params.width * 3.0 / 4.0);
                    vlcVideoLayout.setLayoutParams(params);
                    break;
                case 3: // Stretch
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    vlcVideoLayout.setLayoutParams(params);
                    if (vlcVideoPlayer != null) {
                        vlcVideoPlayer.setAspectRatio(null); // Remove aspect ratio constraints
                    }
                    break;
            }
        }
    }
    
    // Métodos para Picture-in-Picture
    private void startPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
                Rational aspectRatio = new Rational(16, 9);
                PictureInPictureParams.Builder paramsBuilder = new PictureInPictureParams.Builder()
                        .setAspectRatio(aspectRatio);
                        
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    paramsBuilder.setSeamlessResizeEnabled(true);
                }
                
                enterPictureInPictureMode(paramsBuilder.build());
            } else {
                Toast.makeText(this, "Modo PIP não suportado neste dispositivo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Modo PIP requer Android 8.0 ou superior", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        this.isInPictureInPictureMode = isInPictureInPictureMode;
        
        if (isInPictureInPictureMode) {
            // Esconder controles no modo PIP
            playerControlsContainer.setVisibility(View.GONE);
            pauseHideControlsTimer();
        } else {
            // Mostrar controles quando sair do modo PIP
            if (!isFullscreen) {
                showControls();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseHideControlsTimer();
        if (vlcVideoPlayer != null && !isInPictureInPictureMode) {
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