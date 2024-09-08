package com.example.telcobrightcaller.serviceImpl;

import android.content.Context;

import com.example.telcobrightcaller.service.CallUtilService;

import java.util.HashMap;
import java.util.Map;

public class TBCallUtilService implements CallUtilService {
    private Context context;
    private Map<String, Object> callActivities;
    private String currentUserEntityID;
    private String freeSwitchUserId;

    public TBCallUtilService(Context context, String currentUserEntityID, String freeSwitchUserId) {
        this.context = context;
        this.callActivities = new HashMap<>();
        this.currentUserEntityID = currentUserEntityID;
        this.freeSwitchUserId = freeSwitchUserId;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void sendPushNotification(Map<String, Object> data) {
        sendFCMPushNotification(data);
    }

    private void sendFCMPushNotification(Map<String, Object> data) {
    }

    @Override
    public void addCallActivity(String activity, Object object) {
        // Add the activity to your custom call activities map
        callActivities.put(activity, object);
    }

    @Override
    public void removeCallActivity(String activity) {
        callActivities.remove(activity);
    }

    @Override
    public Object getCallActivity(String activity) {
        return callActivities.get(activity);
    }

    @Override
    public String getCurrentUserEntityID() {
        return currentUserEntityID;
    }

    @Override
    public String getFreeSwitchUserId() {
        return freeSwitchUserId;
    }

    @Override
    public void mediaStop() {
        stopMedia();
    }

    private void stopMedia() {
    }
}
