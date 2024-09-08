package com.example.telcobrightcaller;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.telcobrightcaller.models.JanusResponse;
import com.example.telcobrightcaller.utils.NewJanusMessageInterface;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Websocket {
    public WebSocketClient webSocket;
    private String userName;
    private final String TAG = "SocketRepository";
    private final Gson gson = new Gson();
    private final NewJanusMessageInterface messageInterface;
    public Object dynamicClassInstance = new Object();
    public Websocket(NewJanusMessageInterface messageInterface, Object dynamicClassInstance) {
        this.messageInterface = messageInterface;
        this.dynamicClassInstance = dynamicClassInstance;
    }

    public void sendKeepAlive() {
        try {
            Field sessionIdField = dynamicClassInstance.getClass().getField("sessionId");
            long sessionId = (long) sessionIdField.get(dynamicClassInstance);
            System.out.println("sessionId: " + sessionId);
//            System.out.println("sessionId: " + sessionId);

            // Get the TID method from the dynamic class using reflection
            Method tidMethod = dynamicClassInstance.getClass().getMethod("TID");
            String tid = (String) tidMethod.invoke(dynamicClassInstance);

            // Construct the JSON message for sending keep alive
            String keepAliveMessage = "{ \"janus\": \"keepalive\", \"session_id\":" + sessionId + ", \"transaction\":\"" + tid + "\" }";

            // Invoke the sendMessage method of the dynamic class using reflection
            sendMessage(keepAliveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initSocket(String username) {
        try {

            Map<String,String> httpHeaders  = new HashMap<>();
            httpHeaders.put("Sec-Websocket-Protocol","janus-protocol");

//            webSocket = new WebSocketClient(new URI("wss://tb.intercloud.com.bd/"),httpHeaders) {
            webSocket = new WebSocketClient(new URI("wss://103.248.13.76/"),httpHeaders) {
                //            webSocket = new WebSocketClient(new URI("wss://192.168.0.105/"),httpHeaders) {
//            webSocket = new WebSocketClient(new URI("wss://192.168.68.122/"),httpHeaders) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    try {
                        // Invoke the createSession method of the dynamic class using reflection
                        Method createSessionMethod = dynamicClassInstance.getClass().getMethod("createSession");
                        createSessionMethod.invoke(dynamicClassInstance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {

                        messageInterface.onNewMessage(gson.fromJson(message, JanusResponse.class));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    try {
                        Method finishMethod = dynamicClassInstance.getClass().getMethod("finish");
                        finishMethod.invoke(dynamicClassInstance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stopKeepAliveTimer();
                }

                @Override
                public void onError(Exception ex) {
                    try {
                        Method finishMethod = dynamicClassInstance.getClass().getMethod("finish");
                        finishMethod.invoke(dynamicClassInstance);
                        showToast("Error Please Try again");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "onError: " + ex);

                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (webSocket != null) {
            webSocket.connect();
        }
    }

    Timer timer = new Timer("Timer");
    public void startKeepAliveTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                sendKeepAlive();
            }
        };

        long delay = 1000L;
        long interval = 25000L;
        timer.schedule(task, delay, interval);
    }

    public void stopKeepAliveTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
    public void closeSocket() {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.close();
        }
    }

    public void showServiceUnavailableToast() {
        if (dynamicClassInstance != null) {
            ((Activity) dynamicClassInstance).runOnUiThread(() -> {
                Toast.makeText((Context) dynamicClassInstance, "Service Unavailable", Toast.LENGTH_SHORT).show();
            });
        }
    }
    public void showToast(String message) {
        if (dynamicClassInstance != null) {
            ((Activity) dynamicClassInstance).runOnUiThread(() -> {
                Toast.makeText((Context) dynamicClassInstance, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void sendMessage(String message) {
        try {
            Log.d(TAG, "sendMessageToSocket: " + message);
            if (webSocket != null) {

                webSocket.send(message);
            }
        } catch (Exception e) {
            Log.d(TAG, "sendMessageToSocket: " + e);
        }
    }
}
