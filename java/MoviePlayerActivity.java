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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.videolan.libvlc.util.VLCVideoLayout;

public class MoviePlayerActivity extends AppCompatActivity implements VlcVideoPlayer.PlayerControlsListener {

    private VLCVideoLayout vlcVideoLayout;
    private VlcVideoPlayer vlcVideoPlayer;
    private Movie movie;
    private String streamUrl;
    private TextView titleTextView;
    private ImageButton aspectRatioButton;
    private ImageButton pipButton;
    private ImageButton backButton;
    private ImageButton fullscreenButton;
    private ImageButton playPauseButton;
    private ImageButton rewindButton;
    private ImageButton forwardButton;
    private ProgressBar videoLoadingProgressBar;
    private LinearLayout playerControlsContainer;
    private SeekBar progressSeekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    
    private int currentAspectRatio = 0; // 0: Original, 1: 16:9, 2: 4:3, 3: Stretch, 4: Preencher Tela
    private String[] aspectRatioNames = {"Original", "16:9", "4:3", "Esticado", "Preencher Tela"};
    private boolean isInPictureInPictureMode = false;
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
        setupControlsHandling();
        setupListeners();
        startPlayer();
    }
    
    private void initViews() {
        vlcVideoLayout = findViewById(R.id.vlcVideoLayout);
        titleTextView = findViewById(R.id.titleTextView);
        aspectRatioButton = findViewById(R.id.aspectRatioButton);
        pipButton = findViewById(R.id.pipButton);
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        rewindButton = findViewById(R.id.rewindButton);
        forwardButton = findViewById(R.id.forwardButton);
        videoLoadingProgressBar = findViewById(R.id.videoLoadingProgressBar);
        playerControlsContainer = findViewById(R.id.playerControlsContainer);
        progressSeekBar = findViewById(R.id.progressSeekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);

        if (movie.getName() != null) {
            titleTextView.setText(movie.getName());
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

    private void startPlayer() {
        videoLoadingProgressBar.setVisibility(View.VISIBLE);
        
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.stop();
        }
        
        vlcVideoPlayer = new VlcVideoPlayer(this, vlcVideoLayout);
        vlcVideoPlayer.setControlsListener(this);
        vlcVideoPlayer.play(streamUrl);
        
        // Reset aspect ratio para defaults ao iniciar
        currentAspectRatio = 0;
        
        videoLoadingProgressBar.setVisibility(View.GONE);
        startHideControlsTimer();
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
        if (vlcVideoPlayer != null && vlcVideoLayout != null) {
            try {
                // Aguardar um momento para garantir que o player está pronto
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        applyAspectRatioInternal();
                    }
                }, 100);
                
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao alterar proporção: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void applyAspectRatioInternal() {
        if (vlcVideoPlayer != null && vlcVideoLayout != null) {
            try {
                // Reset para defaults primeiro
                vlcVideoPlayer.setScale(0); // 0 = auto scale
                
                switch (currentAspectRatio) {
                    case 0: // Original
                        vlcVideoPlayer.setAspectRatio(null); // null = aspect ratio original
                        vlcVideoPlayer.setScale(0);
                        break;
                    case 1: // 16:9
                        vlcVideoPlayer.setAspectRatio("16:9");
                        vlcVideoPlayer.setScale(0);
                        break;
                    case 2: // 4:3
                        vlcVideoPlayer.setAspectRatio("4:3");
                        vlcVideoPlayer.setScale(0);
                        break;
                    case 3: // Stretch (Esticado)
                        vlcVideoPlayer.setAspectRatio(null);
                        vlcVideoPlayer.setScale(1.0f); // Scale para preencher mantendo proporção
                        break;
                    case 4: // Preencher Tela
                        vlcVideoPlayer.setAspectRatio(null);
                        // Para filme em tela cheia, usar toda a tela
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        int screenHeight = getResources().getDisplayMetrics().heightPixels;
                        // Calcular scale para garantir que o vídeo preencha toda a tela
                        float scale = Math.max(1.2f, Math.max((float)screenWidth / 1920f, (float)screenHeight / 1080f));
                        vlcVideoPlayer.setScale(scale);
                        break;
                }
                
                // Força refresh do layout
                vlcVideoLayout.requestLayout();
                vlcVideoLayout.invalidate();
                
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao alterar proporção: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // Métodos para Picture-in-Picture
    private void startPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
                try {
                    // Verificar se já está em modo PIP
                    if (isInPictureInPictureMode()) {
                        return;
                    }
                    
                    Rational aspectRatio = new Rational(16, 9);
                    PictureInPictureParams.Builder paramsBuilder = new PictureInPictureParams.Builder();
                    
                    // Adicionar parâmetros com validação
                    paramsBuilder.setAspectRatio(aspectRatio);
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        paramsBuilder.setSeamlessResizeEnabled(true);
                    }
                    
                    // Tentar entrar no modo PIP
                    boolean success = enterPictureInPictureMode(paramsBuilder.build());
                    
                    if (!success) {
                        Toast.makeText(this, "Não foi possível ativar o modo PIP", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao ativar modo PIP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        // Não pausar o vídeo quando entrar em modo PIP
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