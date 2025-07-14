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

    public VlcVideoPlayer(Context context, VLCVideoLayout videoLayout) {
        this.videoLayout = videoLayout;
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-sub-autodetect-file");
        options.add("--swscale-mode=0");
        options.add("--network-caching=1500");
        options.add("--sout-mux-caching=1500");
        libVLC = new LibVLC(context, options);
        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.attachViews(videoLayout, null, false, false);
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
}
