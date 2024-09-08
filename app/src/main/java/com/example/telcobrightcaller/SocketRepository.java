package com.example.telcobrightcaller;

import android.util.Log;

import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface;
import com.example.telcobrightcaller.models.JanusResponse;
import com.example.telcobrightcaller.models.MessageModel;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class SocketRepository {
    private WebSocketClient webSocket;
    private String userName;
    private final String TAG = "SocketRepository";
    private final Gson gson = new Gson();
    private final NewMessageInterface messageInterface;

    public SocketRepository(NewMessageInterface messageInterface) {
        this.messageInterface = messageInterface;
    }

    public void initSocket(String username) {
        userName = username;
        try {
            webSocket = new WebSocketClient(new URI("ws://192.168.68.120:3000")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    sendMessageToSocket(new MessageModel("store_user", username, null, null));
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
                    Log.d(TAG, "onClose: " + reason);
                }

                @Override
                public void onError(Exception ex) {
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

    public void sendMessageToSocket(MessageModel message) {
        try {
            Log.d(TAG, "sendMessageToSocket: " + message);
            if (webSocket != null) {
                webSocket.send(gson.toJson(message));
            }
        } catch (Exception e) {
            Log.d(TAG, "sendMessageToSocket: " + e);
        }
    }
}
