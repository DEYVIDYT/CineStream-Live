package com.cinestream.live;

import android.content.Context;
import android.net.Uri;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class VlcVideoPlayer {

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;
    private PlayerControlsListener controlsListener;

    public interface PlayerControlsListener {
        void onPositionChanged(float position);
        void onTimeChanged(long time);
        void onLengthChanged(long length);
        void onPlayingStateChanged(boolean isPlaying);
    }

    public VlcVideoPlayer(Context context, VLCVideoLayout videoLayout) {
        this.videoLayout = videoLayout;
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-sub-autodetect-file");
        options.add("--swscale-mode=0");
        options.add("--network-caching=1500");
        options.add("--sout-mux-caching=1500");
        options.add("--android-display-chroma=RV32"); // Melhor performance em Android
        options.add("--audio-time-stretch"); // Suporte a stretching de áudio
        options.add("--avcodec-skiploopfilter=0"); // Melhor qualidade de vídeo
        libVLC = new LibVLC(context, options);
        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.attachViews(videoLayout, null, false, false);
        
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                if (controlsListener == null) return;
                
                switch (event.type) {
                    case MediaPlayer.Event.TimeChanged:
                        controlsListener.onTimeChanged(event.getTimeChanged());
                        if (getLength() > 0) {
                            float position = (float) event.getTimeChanged() / getLength();
                            controlsListener.onPositionChanged(position);
                        }
                        break;
                    case MediaPlayer.Event.LengthChanged:
                        controlsListener.onLengthChanged(event.getLengthChanged());
                        break;
                    case MediaPlayer.Event.Playing:
                        controlsListener.onPlayingStateChanged(true);
                        break;
                    case MediaPlayer.Event.Paused:
                    case MediaPlayer.Event.Stopped:
                        controlsListener.onPlayingStateChanged(false);
                        break;
                }
            }
        });
    }
    
    public void setControlsListener(PlayerControlsListener listener) {
        this.controlsListener = listener;
    }

    public void play(String url) {
        Media media = new Media(libVLC, Uri.parse(url));
        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
        libVLC.release();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    
    public void setAspectRatio(String aspectRatio) {
        mediaPlayer.setAspectRatio(aspectRatio);
    }
    
    public void setScale(float scale) {
        mediaPlayer.setScale(scale);
    }
    
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    
    public void setVideoTrackEnabled(boolean enabled) {
        mediaPlayer.setVideoTrackEnabled(enabled);
    }
    
    // Métodos para controle de reprodução e tempo
    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.play();
        }
    }
    
    // Controles de tempo e posição
    public long getTime() {
        return mediaPlayer.getTime();
    }
    
    public long getLength() {
        return mediaPlayer.getLength();
    }
    
    public float getPosition() {
        return mediaPlayer.getPosition();
    }
    
    public void setPosition(float position) {
        mediaPlayer.setPosition(position);
    }
    
    public void setTime(long time) {
        mediaPlayer.setTime(time);
    }
    
    // Avançar/Retroceder
    public void seekForward(long milliseconds) {
        long currentTime = getTime();
        long newTime = currentTime + milliseconds;
        long length = getLength();
        if (newTime > length) {
            newTime = length;
        }
        setTime(newTime);
    }
    
    public void seekBackward(long milliseconds) {
        long currentTime = getTime();
        long newTime = currentTime - milliseconds;
        if (newTime < 0) {
            newTime = 0;
        }
        setTime(newTime);
    }
    
    // Formatar tempo em string
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
