package com.example.telcobrightcaller.utils;


import com.example.telcobrightcaller.Websocket;

public class WebSocketSingleton {
    private static WebSocketSingleton instance;
    private Websocket websocket;

    private WebSocketSingleton() {
    }

    public static WebSocketSingleton getInstance() {
        if (instance == null) {
            instance = new WebSocketSingleton();
        }
        return instance;
    }

    public Websocket getWebsocket() {
        return websocket;
    }

    public void setWebsocket(Websocket websocket) {
        this.websocket = websocket;
    }
}