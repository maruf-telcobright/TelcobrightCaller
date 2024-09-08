package com.example.telcobrightcaller.models;

import com.example.telcobrightcaller.utils.NewJanusMessageInterface;

import org.json.JSONException;

public interface JanusCallHandlerInterface extends NewJanusMessageInterface {
    void handleSentMessage(String message);
    void handleReceivedMessage(String message);
    void createSession();
    void hangup();

    void onNewMessage(JanusResponse message) throws JSONException;

//    void sendMessage(String message);
}

