package com.example.telcobrightcaller.service;

public interface PhoneCall {
    public void startSession();
    public void updateSession();
    public void disconnect();
    public void onRing();
    public void onAnswer();
}
