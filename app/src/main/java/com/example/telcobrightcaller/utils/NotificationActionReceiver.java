package com.example.telcobrightcaller.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.example.telcobrightcaller.AudioCallService;
import com.example.telcobrightcaller.RTCClient;


public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_MUTE = "com.codewithkael.webrtcprojectforrecord.ACTION_MUTE";
    public static final String ACTION_END_CALL = "com.codewithkael.webrtcprojectforrecord.ACTION_END_CALL";
    public static final String ACTION_CHANGE_SPEAKER = "com.codewithkael.webrtcprojectforrecord.SPEAKER";
    public static final String ACTION_APP_TO_APP_AUDIO = "com.codewithkael.webrtcprojectforrecord.ACTION_APP_TO_APP_AUDIO";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if(ACTION_APP_TO_APP_AUDIO.equals(action))
            {
                Intent changeNotificationIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_RESUME");
                context.sendBroadcast(changeNotificationIcon);
            }
            else if (ACTION_MUTE.equals(action)) {
                // Handle mute action
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                RTCClient rtcClient = RTCClientSingleton.getInstance().getRtcClient();

                if (audioManager != null) {
                    rtcClient.toggleAudio(true);
//                    audioManager.setMicrophoneMute(!audioManager.isMicrophoneMute());
                    Intent changeIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE");
                    context.sendBroadcast(changeIcon);
                    Intent changeNotificationIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE_NOTIFICATION_ICON");
                    context.sendBroadcast(changeNotificationIcon);
                }
            }
            else if (ACTION_CHANGE_SPEAKER.equals(action)) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(!audioManager.isSpeakerphoneOn());
                }
                Intent changeIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_CHANGE_SPEAKER");
                context.sendBroadcast(changeIcon);
            }
            else if (ACTION_END_CALL.equals(action)) {
                Intent serviceIntent = new Intent(context, AudioCallService.class);
                context.stopService(serviceIntent);
                Intent finishIntent = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY");
                context.sendBroadcast(finishIntent);
            }

        }
    }
}
