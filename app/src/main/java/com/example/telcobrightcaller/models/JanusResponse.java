package com.example.telcobrightcaller.models;

import com.google.gson.annotations.SerializedName;

public class JanusResponse {
    public static JanusResponse.Data Data;
    public static PluginData plugin;
    public static Result result;
    @SerializedName("janus")
    private String janus;

    @SerializedName("session_id")
    private long sessionId = 0;

    @SerializedName("transaction")
    private String transaction;

    @SerializedName("sender")
    public long sender = 0;

    @SerializedName("plugindata")
    private PluginData pluginData;

    @SerializedName("body")
    private Body body;

    @SerializedName("jsep")
    private Jsep jsep;

    // Getters and setters

    public String getJanus() {
        return janus;
    }

    public void setJanus(String janus) {
        this.janus = janus;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
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

//    public void setSender(long sender) {
//        this.sender = sender;
//    }

    public PluginData getPluginData() {
        return pluginData;
    }

    public void setPluginData(PluginData pluginData) {
        this.pluginData = pluginData;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Jsep getJsep() {
        return jsep;
    }

    public void setJsep(Jsep jsep) {
        this.jsep = jsep;
    }
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    @SerializedName("data")
    private Data data;

    // Nested classes

    public static class PluginData {
        @SerializedName("plugin")
        private String plugin;

        @SerializedName("data")
        private Data data;

        // Getters and setters

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

        public Data getPluginData() {
            return data;
        }
    }

    public static class Data {
        @SerializedName("id")
        private long id;
        @SerializedName("sip")
        private String sip;

        @SerializedName("videocall")
        private String videoCall;

        @SerializedName("result")
        private Result result;

        @SerializedName("call_id")
        private String callId;

        @SerializedName("error_code")
        private int errorCode;

        @SerializedName("error")
        private String error;


        // Getters and setters
        public String getVideoCall() {return videoCall;}
        public void setVideoCall(String videoCall) {this.videoCall = videoCall;}
        public long getId() {
            return id;
        }

        public void setId(long id) {
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

        public String getCallId() {
            return callId;
        }

        public void setCallId(String callId) {
            this.callId = callId;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public static class Result {
        @SerializedName("event")
        private String event;

        @SerializedName("code")
        private int code;

        @SerializedName("reason")
        private String reason;

        @SerializedName("username")
        private String username;

        @SerializedName("register_sent")
        private boolean registerSent;

        @SerializedName("master_id")
        private long masterId;

        // Getters and setters

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public boolean isRegisterSent() {
            return registerSent;
        }

        public void setRegisterSent(boolean registerSent) {
            this.registerSent = registerSent;
        }

        public long getMasterId() {
            return masterId;
        }

        public void setMasterId(long masterId) {
            this.masterId = masterId;
        }
    }

    static class Body {
        @SerializedName("request")
        private String request;

        @SerializedName("username")
        private String username;

        @SerializedName("authuser")
        private String authUser;

        @SerializedName("display_name")
        private String displayName;

        @SerializedName("secret")
        private String secret;

        @SerializedName("proxy")
        private String proxy;

        @SerializedName("uri")
        private String uri;

        @SerializedName("autoaccept_reinvites")
        private boolean autoAcceptReinvites;

        // Getters and setters

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

        public String getAuthUser() {
            return authUser;
        }

        public void setAuthUser(String authUser) {
            this.authUser = authUser;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
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

        public boolean isAutoAcceptReinvites() {
            return autoAcceptReinvites;
        }

        public void setAutoAcceptReinvites(boolean autoAcceptReinvites) {
            this.autoAcceptReinvites = autoAcceptReinvites;
        }
    }

    public static class Jsep {
        @SerializedName("type")
        private String type;

        @SerializedName("sdp")
        private String sdp;

        // Getters and setters

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

        // Getters and setters (omitted for brevity)

        static class PluginData {
            @SerializedName("plugin")
            private String plugin;

            @SerializedName("data")
            private Data data;

            // Getters and setters (omitted for brevity)
        }

        static class Data {
            @SerializedName("sip")
            private String sip;

            @SerializedName("videocall")
            private String videoCall;

            @SerializedName("result")
            private Result result;

            @SerializedName("call_id")
            private String callId;

            // Getters and setters (omitted for brevity)
        }

        static class Result {
            @SerializedName("event")
            private String event;

            @SerializedName("code")
            private int code;

            @SerializedName("reason")
            private String reason;

            @SerializedName("username")
            private String username;

            @SerializedName("register_sent")
            private boolean registerSent;

            @SerializedName("master_id")
            private long masterId;

            // Getters and setters (omitted for brevity)
        }

        static class Body {
            @SerializedName("request")
            private String request;

            @SerializedName("username")
            private String username;

            @SerializedName("authuser")
            private String authUser;

            @SerializedName("display_name")
            private String displayName;

            @SerializedName("secret")
            private String secret;

            @SerializedName("proxy")
            private String proxy;

            @SerializedName("uri")
            private String uri;

            @SerializedName("autoaccept_reinvites")
            private boolean autoAcceptReinvites;

            // Getters and setters (omitted for brevity)
        }


        static class Event {
            @SerializedName("event")
            private String event;

            @SerializedName("session_id")
            private long sessionId;

            @SerializedName("transaction")
            private String transaction;

            @SerializedName("sender")
            private long sender;

            @SerializedName("plugindata")
            private PluginData pluginData;

            @SerializedName("jsep")
            private Jsep jsep;

            // Getters and setters (omitted for brevity)
        }
    }

    @Override
    public String toString() {
        return "JanusResponse{" +
                "janus='" + janus + '\'' +
                ", sessionId=" + sessionId +
                ", transaction='" + transaction + '\'' +
                ", sender=" + sender +
                ", pluginData=" + pluginData +
                ", body=" + body +
                ", jsep=" + jsep +
                '}';
    }
}

