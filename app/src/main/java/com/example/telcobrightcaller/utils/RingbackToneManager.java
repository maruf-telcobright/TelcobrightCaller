package com.example.telcobrightcaller.utils;



import static org.webrtc.ContextUtils.getApplicationContext;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.telcobrightcaller.R;
import com.example.telcobrightcaller.service.CallUtilService;
import com.example.telcobrightcaller.serviceImpl.TBCallUtilService;


public class RingbackToneManager {
    private static RingbackToneManager instance;
    private  Context context;
    private MediaPlayer mediaPlayer;
    private static CallUtilService callUtilService;

    private RingbackToneManager() {
        // Private constructor to prevent instantiation
        callUtilService = new TBCallUtilService(getApplicationContext(),"+8801932383889", "+8801932383889");
    }

    public static synchronized RingbackToneManager getInstance() {
        if (instance == null) {
            instance = new RingbackToneManager();
        }

        return instance;
    }

    public void startRingbackTone() {
        // Release any previous instance
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        // Initialize the MediaPlayer with the custom MP3 file
        //mediaPlayer = MediaPlayer.create(callUtilService.getContext(), R.raw.ringback);  //MediaPlayer.create(shared().context(),R.raw.ringback);


        // Set looping if needed
        mediaPlayer.setLooping(true);

        // Start playing the ringback tone
        mediaPlayer.start();
    }

    public void stopRingbackTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
