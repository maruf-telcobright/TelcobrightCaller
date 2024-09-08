package com.example.telcobrightcaller.models;

import com.google.gson.Gson;

public class MessageModel {
    public Success success;
    public Message message;
    public Event event;

    private String type;
    private String name;

    public String getTarget() {
        return target;
    }

    public String getJanus() {
        return this.getJanus();
    }

    private String target;
    private Object data;

    // Constructor
    public MessageModel(String type, String name, String target, Object data) {
        this.type = type;
        this.name = name;
        this.target = target;
        this.data = data;
    }
    public MessageModel() {
    }
//
    public String getType() {
        return this.type;
    }

    public Object getData() {
        return this.data;
    }

    public String getName() {
        return name;
    }

//    // Getters and setters...


//    code by jubair on 14/03/2024

//    create class
    public static class Create{

        public String janus = null;
        public String transaction = null;

        public Create(String janus, String transaction) {
            this.janus = janus;
            this.transaction = transaction;
        }

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }


    }


//    success class
    public class Success{

    public String janus = null;
    public String transaction = null;
    public Data data;
    public long session_id;

    public Success(String janus, String transaction, Data data, long session_id) {
        this.janus = janus;
        this.transaction = transaction;
        this.data = data;
        this.session_id = session_id;
    }

    public String getJanus() {
        return janus;
    }

    public void setJanus(String janus) {
        this.janus = janus;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public long getSession_id() {
        return session_id;
    }

    public void setSession_id(long session_id) {
        this.session_id = session_id;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }

}


//    data class
    public class Data{
        public String id = null;
        public String sip = null;
        public Result result;
        public String call_id;

    public Data(String id, String sip, Result result, String call_id) {
        this.id = id;
        this.sip = sip;
        this.result = result;
        this.call_id = call_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSip() {
        return sip;
    }

    public void setSip(String sip) {
        this.sip = sip;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getCall_id() {
        return call_id;
    }

    public void setCall_id(String call_id) {
        this.call_id = call_id;
    }


    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }


}

//attach class

    public static class Attach{
        public String janus = null;
        public String plugin = null;
        public String opaque_id = null;
        public String transaction = null;
        public  long session_id = 0;

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public String getPlugin() {
            return plugin;
        }

        public void setPlugin(String plugin) {
            this.plugin = plugin;
        }

        public String getOpaque_id() {
            return opaque_id;
        }

        public void setOpaque_id(String opaque_id) {
            this.opaque_id = opaque_id;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public Attach(String janus, String plugin, String opaque_id, String transaction, long session_id) {
            this.janus = janus;
            this.plugin = plugin;
            this.opaque_id = opaque_id;
            this.transaction = transaction;
            this.session_id = session_id;
        }


        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }


    }

//    message class

    public static class Message{
        public String janus = null;
        public Body body;
        public String transaction;
        public long session_id;
        public long handle_id;
        public Jsep jsep;

        public Message(String janus, Body body, String transaction, long session_id, long handle_id, Jsep jsep) {
            this.janus = janus;
            this.body = body;
            this.transaction = transaction;
            this.session_id = session_id;
            this.handle_id = handle_id;
            this.jsep = jsep;
        }

        public Message(String janus, Body body, String transaction, long session_id, long handle_id) {
            this.janus = janus;
            this.body = body;
            this.transaction = transaction;
            this.session_id = session_id;
            this.handle_id = handle_id;
        }



        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public long getHandle_id() {
            return handle_id;
        }

        public void setHandle_id(long handle_id) {
            this.handle_id = handle_id;
        }

        public Jsep getJsep() {
            return jsep;
        }

        public void setJsep(Jsep jsep) {
            this.jsep = jsep;
        }
    }


//    class body
    public static class Body{
        public String request = null;
        public String username = null;
        public String authuser = null;
        public String display_name = null;
        public String secret = null;
        public String proxy = null;
        public String uri = null;
        public boolean autoaccept_reinvites ;

    public Body(String request, String username, String authuser, String display_name, String secret, String proxy) {
        this.request = request;
        this.username = username;
        this.authuser = authuser;
        this.display_name = display_name;
        this.secret = secret;
        this.proxy = proxy;
    }

    public Body(String request, String uri, boolean autoaccept_reinvites) {
        this.request = request;
        this.uri = uri;
        this.autoaccept_reinvites = autoaccept_reinvites;
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthuser() {
        return authuser;
    }

    public void setAuthuser(String authuser) {
        this.authuser = authuser;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean getAutoaccept_reinvites() {
        return autoaccept_reinvites;
    }

    public void setAutoaccept_reinvites(boolean autoaccept_reinvites) {
        this.autoaccept_reinvites = autoaccept_reinvites;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }
}

// class event
public class Event{
        public String janus;
        public long session_id;
        public String transaction;
        public long sender;
        public Plugindata plugindata;

        public Jsep jsep;

    public Event(String janus, long session_id, String transaction, long sender, Plugindata plugindata) {
        this.janus = janus;
        this.session_id = session_id;
        this.transaction = transaction;
        this.sender = sender;
        this.plugindata = plugindata;
    }

    public String getJanus() {
        return janus;
    }

    public void setJanus(String janus) {
        this.janus = janus;
    }

    public long getSession_id() {
        return session_id;
    }

    public void setSession_id(long session_id) {
        this.session_id = session_id;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public Plugindata getPlugindata() {
        return plugindata;
    }

    public void setPlugindata(Plugindata plugindata) {
        this.plugindata = plugindata;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }
}


// result class

public class Result{
        public String event = null;
        public String username = null;
        public String register_sent = null;
        public long master_id = 0;

        public String call_id = null;
        public String code = null;

        public String reason = null;

    public Result(String event, String username, String register_sent, long master_id, String call_id, String code, String reason) {
        this.event = event;
        this.username = username;
        this.register_sent = register_sent;
        this.master_id = master_id;
        this.call_id = call_id;
        this.code = code;
        this.reason = reason;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegister_sent() {
        return register_sent;
    }

    public void setRegister_sent(String register_sent) {
        this.register_sent = register_sent;
    }

    public long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(long master_id) {
        this.master_id = master_id;
    }

    public String getCall_id() {
        return call_id;
    }

    public void setCall_id(String call_id) {
        this.call_id = call_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }


}


// plugindata class

public class Plugindata{
        public String plugin = null;
        public Data data;

    public Plugindata(String plugin, Data data) {
        this.plugin = plugin;
        this.data = data;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }

}

// class jsep

    public static class Jsep{
        public String type = null;
        public String sdp = null;

        public Jsep(String type, String sdp) {
            this.type = type;
            this.sdp = sdp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSdp() {
            return sdp;
        }

        public void setSdp(String sdp) {
            this.sdp = sdp;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }
    }


// class trickle
public static class Trickle{
        public String janus = null;
        public Candidate candidate;
        public String transaction = null;
        public long session_id = 0;
        public long handle_id = 0;

    public Trickle(String janus, Candidate candidate, String transaction, long session_id, long handle_id) {
        this.janus = janus;
        this.candidate = candidate;
        this.transaction = transaction;
        this.session_id = session_id;
        this.handle_id = handle_id;
    }

    public String getJanus() {
        return janus;
    }

    public void setJanus(String janus) {
        this.janus = janus;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public long getSession_id() {
        return session_id;
    }

    public void setSession_id(long session_id) {
        this.session_id = session_id;
    }

    public long getHandle_id() {
        return handle_id;
    }

    public void setHandle_id(long handle_id) {
        this.handle_id = handle_id;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }

}

// class candidate
    public class Candidate{
        public String candidate = null;
        public String sdpMid = null;
        public String sdpMLineIndex = null;

        public boolean completed = false;

    public Candidate(String candidate, String sdpMid, String sdpMLineIndex, boolean completed) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.completed = completed;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public String getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(String sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public  String toString(){
        return  "";
    }

    public Gson toJson(){
        return new Gson();
    }

}


// class ack

    public class Ack{
        public String janus = null;
        public long session_id = 0;
        public String transaction = null;

        public Ack(String janus, long session_id, String transaction) {
            this.janus = janus;
            this.session_id = session_id;
            this.transaction = transaction;
        }

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }

    }


// class keepalive

    public static class Keepalive{
        public String janus = null;
        public long session_id = 0;
        public String transaction = null;

        public Keepalive(String janus, long session_id, String transaction) {
            this.janus = janus;
            this.session_id = session_id;
            this.transaction = transaction;
        }

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }

    }


// class webrtcup

    public class Webrtcup{
        public String janus = null;
        public long session_id = 0;
        public long sender = 0;

        public Webrtcup(String janus, long session_id, long sender) {
            this.janus = janus;
            this.session_id = session_id;
            this.sender = sender;
        }

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }


    }


// class media

    public class Media{
        public String janus = null;
        public long session_id = 0;
        public long sender = 0;
        public String mid = null;
        public String type = null;
        public String receiving = null;

        public Media(String janus, long session_id, long sender, String mid, String type, String receiving) {
            this.janus = janus;
            this.session_id = session_id;
            this.sender = sender;
            this.mid = mid;
            this.type = type;
            this.receiving = receiving;
        }

        public String getJanus() {
            return janus;
        }

        public void setJanus(String janus) {
            this.janus = janus;
        }

        public long getSession_id() {
            return session_id;
        }

        public void setSession_id(long session_id) {
            this.session_id = session_id;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public String getMid() {
            return mid;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReceiving() {
            return receiving;
        }

        public void setReceiving(String receiving) {
            this.receiving = receiving;
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }


    }


    public static class BodyBuilder {
        private String request;
        private String username;
        private String authuser;
        private String display_name;
        private String secret;
        private String proxy;

        public BodyBuilder(String request, String username) {
            this.request = request;
            this.username = username;
        }

        public BodyBuilder authuser(String authuser) {
            this.authuser = authuser;
            return this;
        }

        public BodyBuilder displayName(String display_name) {
            this.display_name = display_name;
            return this;
        }

        public BodyBuilder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public BodyBuilder proxy(String proxy) {
            this.proxy = proxy;
            return this;
        }

        public Body build() {
            return new Body(request, username, authuser, display_name, secret, proxy);
        }

        public  String toString(){
            return  "";
        }

        public Gson toJson(){
            return new Gson();
        }

    }

    public static Message buildMessage(String janus, Body body, String transaction, long session_id, long handle_id) {
        return new Message(janus, body, transaction, session_id, handle_id, null);
    }
    public static String buildMessagetoString()
    {
        return "MESSAGE";
    }

}