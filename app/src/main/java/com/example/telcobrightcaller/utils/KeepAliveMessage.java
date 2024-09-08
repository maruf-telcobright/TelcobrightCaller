package com.example.telcobrightcaller.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.example.telcobrightcaller.Websocket;

import java.util.Random;

public  class KeepAliveMessage  extends BroadcastReceiver {

    public static Websocket websocket;
    public static void setWebsocket(Websocket websocket)
    {
        KeepAliveMessage.websocket =websocket;
    }

    private String TID()
    {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 12;
        Random random = new Random();
        String transactionID = new String();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            transactionID+=(characters.charAt(index));
        }
        return transactionID;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
