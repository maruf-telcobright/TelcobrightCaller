package com.example.telcobrightcaller.service;

import android.content.Context;

import java.util.Map;

public interface CallUtilService {
    Context getContext();
    void sendPushNotification(Map<String, Object> data);
    void addCallActivity(String activity, Object object);
    void removeCallActivity(String activity);
    Object getCallActivity(String activity);
    String getCurrentUserEntityID();
    String getFreeSwitchUserId();
    void mediaStop();
}
