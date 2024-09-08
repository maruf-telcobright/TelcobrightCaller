package com.example.telcobrightcaller.service;

import com.example.telcobrightcaller.RTCClient;
import com.example.telcobrightcaller.Websocket;
import com.example.telcobrightcaller.databinding.ActivityCallBinding;

public class JanusCall extends AbstractPhoneCall{
    private Websocket websocket;
    private RTCClient rtcClient;
    public JanusCall(String callId, String calledNumber, String callingNumber) {
        super(callId, calledNumber, callingNumber);
    }
    public void startSession(){

    };
    public void updateSession(){}
    public void disconnect(){}
    public void onRing(){}
    public void onAnswer(){}
}
