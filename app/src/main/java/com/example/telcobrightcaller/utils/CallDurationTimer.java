package com.example.telcobrightcaller.utils;

import android.os.Handler;

public class CallDurationTimer {
    private Handler handler;

    private long startTime;

    public CallDurationTimer() {

        handler = new Handler();
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        handler.post(updateTimerRunnable);
    }

    public void stopTimer() {
        handler.removeCallbacks(updateTimerRunnable);
    }

    private final Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long seconds = elapsedTime / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            // Format the time
            String timeString = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
            // Update the TextView with the formatted time

            // Update again after 1 second
            handler.postDelayed(this, 1000);
        }
    };
}
