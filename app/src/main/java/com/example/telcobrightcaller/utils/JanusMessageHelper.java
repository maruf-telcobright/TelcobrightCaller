package com.example.telcobrightcaller.utils;


import com.example.telcobrightcaller.models.MessageModel;

public class JanusMessageHelper {
    public static MessageModel.Message buildSdpMessage(String janus, String request, String uri, boolean autoaccept_reinvites,
                                                       String transaction, String type, String sdp, long session_id, long handle_id) {
        MessageModel.Body body = new MessageModel.Body(request, uri, autoaccept_reinvites);
        MessageModel.Jsep jsep = new MessageModel.Jsep(type, sdp);
        return new MessageModel.Message(janus, body, transaction, session_id, handle_id, jsep);
    }
    public static MessageModel.Attach buildAttachMessage(String janus, String plugin, String opaque_id, String transaction, long session_id) {

        return new MessageModel.Attach(janus, plugin, opaque_id, transaction, session_id);
    }
    public static MessageModel.Trickle buildTrickleMessage(String janus, MessageModel.Candidate candidate, String transaction, long session_id, long handle_id) {

        return new MessageModel.Trickle(janus, candidate, transaction, session_id, handle_id);
    }
    public static MessageModel.Message buildSipRegisterMessage(String janus, String request, String username, String authuser, String display_name, String secret, String proxy, String transaction, long session_id, long handle_id) {
        MessageModel.Body body = new MessageModel.Body(request, username, authuser, display_name, secret, proxy);
        return new MessageModel.Message(janus, body, transaction, session_id, handle_id);
    }

    public static MessageModel.Create buildCreateMessage(String janus, String transaction) {
        return new MessageModel.Create(janus, transaction);
    }

    public static MessageModel.Keepalive buildKeepAliveMessage(String janus, long session_id , String transaction) {
        return new MessageModel.Keepalive(janus, session_id, transaction);
    }

}
