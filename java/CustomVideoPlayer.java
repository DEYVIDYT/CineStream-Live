package com.vplay.live;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import java.io.IOException;

public class CustomVideoPlayer implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private Context context;
    private String videoUrl;
    private VideoPlayerListener listener;

    public CustomVideoPlayer(Context context, SurfaceHolder surfaceHolder, String videoUrl) {
        this.context = context;
        this.surfaceHolder = surfaceHolder;
        this.videoUrl = videoUrl;
        this.surfaceHolder.addCallback(this);
    }

    public void setVideoPlayerListener(VideoPlayerListener listener) {
        this.listener = listener;
    }

    private void initializePlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(context, Uri.parse(videoUrl));
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError();
            }
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initializePlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not needed for this implementation
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        release();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (listener != null) {
            listener.onPrepared();
        }
        play();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (listener != null) {
            listener.onError();
        }
        release();
        return true;
    }

    public interface VideoPlayerListener {
        void onPrepared();
        void onError();
    }
}
