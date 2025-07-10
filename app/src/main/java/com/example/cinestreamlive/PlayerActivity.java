package com.example.cinestreamlive;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private ProgressBar playerProgressBar;
    private String channelUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        playerProgressBar = findViewById(R.id.playerProgressBar);

        channelUrl = getIntent().getStringExtra("channel_url");

        if (channelUrl != null && !channelUrl.isEmpty()) {
            playVideo(channelUrl);
        } else {
            // Tratar erro de URL inválida, talvez fechar a activity ou mostrar uma mensagem
            finish();
        }
    }

    private void playVideo(String url) {
        playerProgressBar.setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> {
            playerProgressBar.setVisibility(View.GONE);
            mp.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            playerProgressBar.setVisibility(View.GONE);
            // Tratar erro de reprodução
            // Exibir mensagem para o usuário
            finish(); // Ou alguma outra lógica de tratamento de erro
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se necessário, adicionar lógica para resumir o vídeo
        // if (!videoView.isPlaying() && channelUrl != null) {
        // videoView.start();
        // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }
}
