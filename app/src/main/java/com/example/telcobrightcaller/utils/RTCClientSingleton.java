package com.example.telcobrightcaller.utils;


import com.example.telcobrightcaller.RTCClient;

public class RTCClientSingleton {
    private static RTCClientSingleton instance;
    private RTCClient rtcClient;

    private RTCClientSingleton() {
    }

    public static RTCClientSingleton getInstance() {
        if (instance == null) {
            instance = new RTCClientSingleton();
        }
        return instance;
    }

    public RTCClient getRtcClient() {
        return rtcClient;
    }

    public void setRtcClient(RTCClient rtcClient) {
        this.rtcClient = rtcClient;
    }
}
