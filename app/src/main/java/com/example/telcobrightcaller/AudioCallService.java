package com.example.telcobrightcaller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.example.telcobrightcaller.utils.NotificationActionReceiver;
import com.example.telcobrightcaller.utils.RTCAudioManager;
import com.example.telcobrightcaller.utils.RTCClientSingleton;

import java.util.Timer;

public class AudioCallService extends Service {
    private static final String CHANNEL_ID = "AudioCallServiceChannel";
    private RTCClient rtcClient;
    private RTCAudioManager rtcAudioManager;
    private Timer timer;
    private long startTime;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private PowerManager.WakeLock wakeLock;
    private  Intent intent;
    private  boolean isMuted = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

             if("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE_NOTIFICATION_ICON".equals(intent.getAction()))
            {
                isMuted = !isMuted;
                updateNotification("Audio call in progress",isMuted);

            }
        }
    };

    private void  updateNotification(String content,boolean isMuted) {
        Notification notification = getNotification(content,isMuted);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE_NOTIFICATION_ICON");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_CHANGE_SPEAKER_NOTIFICATION_ICON");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_APP_TO_APP_AUDIO");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_APP_TO_APP_VIDEO");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_APP_TO_APP_SIP");
        registerReceiver(broadcastReceiver, filter);
        createNotificationChannel();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        startForeground(1, getNotification("Audio call in progress",isMuted));
        rtcClient = RTCClientSingleton.getInstance().getRtcClient();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioFocusRequest focusRequest = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build())
                    .build();
        }
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(focusRequest);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(focusRequest);
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioManager.setMicrophoneMute(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.getAvailableCommunicationDevices().stream().findFirst();
            }
        }

        return START_NOT_STICKY;
    }



    private Notification getNotification(String content,boolean isMuted) {
        int icon = isMuted ? R.drawable.ic_baseline_mic_off_24:R.drawable.ic_baseline_mic_24;
        // Create an Intent to launch the app
        Intent launchIntent = new Intent(this, NotificationActionReceiver.class);

        switch (intent.getComponent().getClassName())
        {
            case "com.codewithkael.webrtcprojectforrecord.AudioCallService":
            {
                launchIntent.setAction(NotificationActionReceiver.ACTION_APP_TO_APP_AUDIO);
            }
            break;
        }
        PendingIntent launchPendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent muteIntent = new Intent(this, NotificationActionReceiver.class);
        muteIntent.setAction(NotificationActionReceiver.ACTION_MUTE);
        PendingIntent mutePendingIntent = PendingIntent.getBroadcast(this, 0, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent endCallIntent = new Intent(this, NotificationActionReceiver.class);
        endCallIntent.setAction(NotificationActionReceiver.ACTION_END_CALL);
        PendingIntent endCallPendingIntent = PendingIntent.getBroadcast(this, 0, endCallIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Person incomingCaller = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String  receiverNumber = this.intent.getStringExtra("receiverNumber");
            incomingCaller = new Person.Builder()
                    .setName(receiverNumber)
                    .setImportant(true)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Call In Progress")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOngoing(true)
                    .setStyle(
                            Notification.CallStyle.forOngoingCall(incomingCaller, endCallPendingIntent))
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(launchPendingIntent)
                    .addAction(new Notification.Action.Builder(icon, "Mute", mutePendingIntent).build());
//                    .addAction(new Notification.Action.Builder(R.drawable.baseline_call_end_24_red, "End Call", endCallPendingIntent)
//                    .build());

            return builder.build();
        }
        else {
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Call In Progress")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOngoing(true)
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(launchPendingIntent)
                    .addAction(icon, "Mute", mutePendingIntent)
//                    .addAction(R.drawable.baseline_call_end_24_red, "End Call", endCallPendingIntent)
                    .build();
        }
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for ongoing calls");
            channel.setSound(null, null); // Disable sound for the channel
            channel.getLockscreenVisibility();
            channel.setImportance( NotificationManager.IMPORTANCE_LOW);

            // Register the channel with the system
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("NotificationChannel", "Notification channel created with sound disabled.");
            }
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
