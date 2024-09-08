package com.example.telcobrightcaller.utils;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.telcobrightcaller.service.CallUtilService;
import com.example.telcobrightcaller.serviceImpl.TBCallUtilService;

import org.webrtc.ThreadUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class RTCAudioManager implements Serializable {

    public enum AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, NONE
    }

    public enum AudioManagerState {
        UNINITIALIZED, PREINITIALIZED, RUNNING
    }

    public interface AudioManagerEvents {
        void onAudioDeviceChanged(AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }

    private static final String TAG = "RTCAudioManager";

    private final Context apprtcContext;
    private final AudioManager audioManager;

    @Nullable
    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState amState;
    private int savedAudioMode;
    private boolean savedIsSpeakerPhoneOn;
    private boolean savedIsMicrophoneMute;
    private boolean hasWiredHeadset;

    private AudioDevice defaultAudioDevice;
    private AudioDevice selectedAudioDevice;
    private AudioDevice userSelectedAudioDevice;
    private String useSpeakerphone;

    private Set<AudioDevice> audioDevices = new HashSet<>();

    private final WiredHeadsetReceiver wiredHeadsetReceiver = new WiredHeadsetReceiver();
    @Nullable
    private OnAudioFocusChangeListener audioFocusChangeListener;
    private static CallUtilService callUtilService;

    public RTCAudioManager(Context context) {
        callUtilService = new TBCallUtilService(getApplicationContext(),"+8801932383889", "+8801932383889");
        Log.d(TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        apprtcContext = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        amState = AudioManagerState.UNINITIALIZED;
        useSpeakerphone = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_speakerphone_key", "auto");

        if ("false".equals(useSpeakerphone)) {
            defaultAudioDevice = AudioDevice.EARPIECE;
        } else {
            defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        }
        Log.d(TAG, "useSpeakerphone: " + useSpeakerphone);
        Log.d(TAG, "defaultAudioDevice: " + defaultAudioDevice);
    }

    public static RTCAudioManager create(Context context) {
        return new RTCAudioManager(context);
    }

    public void start(AudioManagerEvents audioManagerEvents) {
        Log.d(TAG, "start");
        ThreadUtils.checkIsOnMainThread();
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active");
            return;
        }
        Log.d(TAG, "AudioManager starts...");
        this.audioManagerEvents = audioManagerEvents;
        amState = AudioManagerState.RUNNING;

        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadset();

        audioFocusChangeListener = focusChange -> {
            String typeOfChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    typeOfChange = "AUDIOFOCUS_GAIN";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    typeOfChange = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                default:
                    typeOfChange = "AUDIOFOCUS_INVALID";
                    break;
            }
            Log.d(TAG, "onAudioFocusChange: " + typeOfChange);
        };

        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(TAG, "Audio focus request failed");
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setMicrophoneMute(false);

        userSelectedAudioDevice = AudioDevice.NONE;
        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();

        updateAudioDeviceState();
        registerReceiver(wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        Log.d(TAG, "AudioManager started");
    }

    @SuppressLint("WrongConstant")
    public void stop() {
        Log.d(TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (amState != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: " + amState);
            return;
        }
        amState = AudioManagerState.UNINITIALIZED;
        unregisterReceiver(wiredHeadsetReceiver);

        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        audioFocusChangeListener = null;
        Log.d(TAG, "Abandoned audio focus for VOICE_CALL streams");

        audioManagerEvents = null;
        Log.d(TAG, "AudioManager stopped");
    }

    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(TAG, "setAudioDeviceInternal(device=" + device + ")");
        if (audioDevices.contains(device)) {
            switch (device) {
                case SPEAKER_PHONE:
                    setSpeakerphoneOn(true);
                    break;
                case EARPIECE:
                    setSpeakerphoneOn(false);
                    break;
                case WIRED_HEADSET:
                    setSpeakerphoneOn(false);
                    break;
                default:
                    Log.e(TAG, "Invalid audio device selection");
                    break;
            }
        }
        selectedAudioDevice = device;
    }

    public void setDefaultAudioDevice(AudioDevice defaultDevice) {
        ThreadUtils.checkIsOnMainThread();
        AudioManager audioManager = (AudioManager) callUtilService.getContext().getSystemService(Context.AUDIO_SERVICE);  //(AudioManager) ChatSDK.ctx().getSystemService(Context.AUDIO_SERVICE);

        switch (defaultDevice) {
            case SPEAKER_PHONE:
                audioManager.setSpeakerphoneOn(true);
                defaultAudioDevice = defaultDevice;
                break;
            case EARPIECE:
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    defaultAudioDevice = defaultDevice;
                break;
            default:
                Log.e(TAG, "Invalid default audio device selection");
        }
        Log.d(TAG, "setDefaultAudioDevice(device=" + defaultAudioDevice + ")");
        updateAudioDeviceState();
    }

    private boolean hasEarpiece(AudioManager audioManager) {
        return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
    }

    public void selectAudioDevice(AudioDevice device) {
        ThreadUtils.checkIsOnMainThread();
        if (!audioDevices.contains(device)) {
            Log.e(TAG, "Can not select " + device + " from available " + audioDevices);
        }
        userSelectedAudioDevice = device;
        updateAudioDeviceState();
    }

    public Set<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet<>(audioDevices));
    }

    public AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return selectedAudioDevice;
    }

    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        apprtcContext.registerReceiver(receiver, filter);
    }

    private void unregisterReceiver(BroadcastReceiver receiver) {
        apprtcContext.unregisterReceiver(receiver);
    }

    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn != on) {
            audioManager.setSpeakerphoneOn(on);
        }
    }

    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted != on) {
            audioManager.setMicrophoneMute(on);
        }
    }

    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn();
        } else {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
                if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    return true;
                }
            }
            return false;
        }
    }

    private void updateAudioDeviceState() {
        Log.d(TAG, "--- updateAudioDeviceState ---");
        Log.d(TAG, "Devices: " + audioDevices + ", selected: " + selectedAudioDevice);
        Log.d(TAG, "Device status: available=" + audioDevices + ", selected=" + selectedAudioDevice +
                ", user selected=" + userSelectedAudioDevice);

        Set<AudioDevice> newAudioDevices = new HashSet<>();
        if (hasWiredHeadset) {
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE);
            }
        }
        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);
        audioDevices = newAudioDevices;

        if (hasWiredHeadset) {
            selectedAudioDevice = AudioDevice.WIRED_HEADSET;
        } else {
            if (userSelectedAudioDevice != AudioDevice.NONE) {
                selectedAudioDevice = userSelectedAudioDevice;
            } else {
                selectedAudioDevice = defaultAudioDevice;
            }
        }

        if (audioDeviceSetUpdated) {
            Log.d(TAG, "New device status: available=" + audioDevices + ", selected=" + selectedAudioDevice);
            if (audioManagerEvents != null) {
                audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState done ---");
    }

    private class WiredHeadsetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            int microphone = intent.getIntExtra("microphone", 0);
            String name = intent.getStringExtra("name");

            hasWiredHeadset = (state == 1);
            Log.d(TAG, "WiredHeadsetReceiver.onReceive: " +
                    "a=" + intent.getAction() +
                    ", s=" + (state == 0 ? "unplugged" : "plugged") +
                    ", m=" + (microphone == 1 ? "mic" : "no mic") +
                    ", n=" + name + ", sb=" + isInitialStickyBroadcast());
            updateAudioDeviceState();
        }
    }
}
