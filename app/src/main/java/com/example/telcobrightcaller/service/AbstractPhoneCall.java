package com.example.telcobrightcaller.service;

public abstract class AbstractPhoneCall implements PhoneCall{
    public String callId;
    public String calledNumber;
    public String callingNumber;

    public AbstractPhoneCall(String callId, String calledNumber, String callingNumber) {
        this.callId = callId;
        this.calledNumber = calledNumber;
        this.callingNumber = callingNumber;
    }
    public void startSession(){};
    public void updateSession(){}
    public void disconnect(){}
    public void onRing(){}
    public void onAnswer(){}
}
