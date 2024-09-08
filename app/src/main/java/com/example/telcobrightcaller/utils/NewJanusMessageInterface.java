package com.example.telcobrightcaller.utils;


import com.example.telcobrightcaller.models.JanusResponse;

import org.json.JSONException;

import java.io.IOException;

public interface NewJanusMessageInterface {
    void onNewMessage(JanusResponse message) throws JSONException, IOException;
}
