package com.example.telcobrightcaller.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@JsonPropertyOrder({ "janus", "body", "candidate", "transaction", "jsep", "session_id", "handle_id" })
public class JanusMessage {
    public static JanusResponse.Jsep Jsep;
    @JsonProperty("janus")
    private String janus;

    @JsonProperty("body")
    private Body body;

    @JsonProperty("transaction")
    private String transaction;

    @JsonProperty("jsep")
    private Jsep jsep;

    @JsonProperty("session_id")
    private long sessionId;

    @JsonProperty("handle_id")
    private long handleId;

    public JanusMessage(String janus, Candidate candidate, String transaction, long sessionId, long handleId) {
        this.janus = janus;
        this.candidate = candidate;
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
    }

    public JanusMessage(String janus, Body body, String transaction, long sessionId, long handleId) {
        this.janus = janus;
        this.body = body;
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
    }

    @JsonPropertyOrder({ "request", "username","uri", "autoaccept_reinvites" })
    public static class Body {
        @JsonProperty("request")
        private String request;

        @JsonProperty("uri")
        private String uri;

        @JsonProperty("username")
        private String username;

        @JsonProperty("autoaccept_reinvites")
        private boolean autoacceptReinvites;

        public Body(String request, String uri, boolean autoacceptReinvites) {
            this.request = request;
            this.uri = uri;
            this.autoacceptReinvites = autoacceptReinvites;
        }

        public Body(String request) {
            this.request = request;
        }

        public Body(String request, String username) {
            this.request = request;
            this.username = username;
        }
    }
    @JsonPropertyOrder({ "type", "sdp" })
    public static class Jsep {
        @JsonProperty("type")
        private String type;

        @JsonProperty("sdp")
        private String sdp;

        public String getSdp() {
            return sdp;
        }

        public void setSdp(String sdp) {
            this.sdp = sdp;
        }

        public Jsep(String type, String sdp) {
            this.type = type;
            this.sdp = sdp;
        }
    }
    public JanusMessage(String janus, Body body, String transaction, Jsep jsep, long sessionId, long handleId) {
        this.janus = janus;
        this.body = body;
        this.transaction = transaction;
        this.jsep = jsep;
        this.sessionId = sessionId;
        this.handleId = handleId;
    }

    public JanusMessage(String janus, Body body, String transaction, Jsep jsep, long sessionId, long handleId, Candidate candidate) {
        this.janus = janus;
        this.body = body;
        this.transaction = transaction;
        this.jsep = jsep;
        this.sessionId = sessionId;
        this.handleId = handleId;
        this.candidate = candidate;
    }

    @JsonProperty("candidate")
    private Candidate candidate;

    @JsonPropertyOrder({ "candidate", "sdpMid", "sdpMLineIndex"})
    public static class Candidate {
        @JsonProperty("candidate")
        private String candidate;

        @JsonProperty("sdpMid")
        private String sdpMid;

        @JsonProperty("sdpMLineIndex")
        private Integer sdpMLineIndex = null;

        @JsonProperty("completed")
        private Boolean completed = null;

        public Candidate(String candidate, String sdpMid, int sdpMLineIndex) {
            this.candidate = candidate;
            this.sdpMid = sdpMid;
            this.sdpMLineIndex = sdpMLineIndex;
        }
        public Candidate(boolean completed) {
            this.completed = completed;
            this.candidate = null;
            this.sdpMid = null;
            this.sdpMLineIndex = null;
        }
    }

    public String toJson(JanusMessage message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(message);
    }



}
