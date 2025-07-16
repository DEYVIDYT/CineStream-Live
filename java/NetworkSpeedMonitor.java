package com.cinestream.live;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;

public class NetworkSpeedMonitor {

    private Handler handler;
    private Runnable runnable;
    private long lastRxBytes = 0;
    private long lastTxBytes = 0;
    private long lastTime = 0;
    private SpeedListener listener;

    public NetworkSpeedMonitor(SpeedListener listener) {
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        this.runnable = new Runnable() {
            @Override
            public void run() {
                calculateSpeed();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
    }

    public void start() {
        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastTxBytes = TrafficStats.getTotalTxBytes();
        lastTime = System.currentTimeMillis();
        handler.post(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
    }

    private void calculateSpeed() {
        long currentRxBytes = TrafficStats.getTotalRxBytes();
        long currentTxBytes = TrafficStats.getTotalTxBytes();
        long currentTime = System.currentTimeMillis();

        long rxBytes = currentRxBytes - lastRxBytes;
        long txBytes = currentTxBytes - lastTxBytes;
        long time = currentTime - lastTime;

        if (time > 0) {
            double rxSpeed = (double) rxBytes / time * 1000; // bytes per second
            double txSpeed = (double) txBytes / time * 1000; // bytes per second
            if (listener != null) {
                listener.onSpeedChanged(rxSpeed, txSpeed);
            }
        }

        lastRxBytes = currentRxBytes;
        lastTxBytes = currentTxBytes;
        lastTime = currentTime;
    }

    public interface SpeedListener {
        void onSpeedChanged(double downloadSpeed, double uploadSpeed);
    }
}
