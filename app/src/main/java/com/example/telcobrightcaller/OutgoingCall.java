package com.example.telcobrightcaller;


import static com.example.telcobrightcaller.utils.NumberStringFormater.reformatPhoneNumber;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.telcobrightcaller.databinding.ActivityCallBinding;
import com.example.telcobrightcaller.models.JanusCallHandlerInterface;
import com.example.telcobrightcaller.models.JanusMessage;
import com.example.telcobrightcaller.models.JanusResponse;
import com.example.telcobrightcaller.service.CallUtilService;
import com.example.telcobrightcaller.serviceImpl.TBCallUtilService;
import com.example.telcobrightcaller.utils.NumberStringFormater;
import com.example.telcobrightcaller.utils.PeerConnectionObserver;
import com.example.telcobrightcaller.utils.RTCAudioManager;
import com.example.telcobrightcaller.utils.RTCClientSingleton;
import com.google.gson.Gson;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class OutgoingCall extends AppCompatActivity implements JanusCallHandlerInterface {


    private static Timer timer;
    private static long startTime;
    private Websocket websocket;
    public static long sessionId = 0;
    public long handleId = 0;
    private int step = -1;
    private static String receiverNumber;
    private ActivityCallBinding binding;
    private String userName;
    private String receiver;
    private RTCClient rtcClient;
    private String TAG = "OutgoingCall";
    private String target = "";
    private Gson gson = new Gson();
    private boolean isMute = false ;
    private boolean isCameraPause = false;
    private RTCAudioManager rtcAudioManager;
    private boolean isSpeakerMode = false;
    private static String type = "audio";
    private static final int PERMISSION_REQUEST_CODE = 1;
    SQLiteCallFragmentHelper sqLiteCallFragmentHelper;
    private static CallUtilService callUtilService;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY".equals(intent.getAction())) {
                runOnUiThread(() -> {
                    hangup();
                });
                rtcClient.endCall();
                finish();
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_CHANGE_SPEAKER".equals(intent.getAction()))
            {
                isSpeakerMode = !isSpeakerMode;
                if (isSpeakerMode) {
                    binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24);
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);
                } else {
                    binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24);
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
                }
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE".equals(intent.getAction()))
            {
                isMute = !isMute;
                if (isMute) {
                    binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                } else {
                    binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                }
                rtcClient.toggleAudio(isMute);
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_RESUME".equals(intent.getAction()))
            {
                onResume();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callUtilService = new TBCallUtilService(getApplicationContext(),"+8801743801850@localhost", "09638122233");
        receiverNumber = NumberStringFormater.normalizePhoneNumber(getIntent().getStringExtra("receiverNumber"));
        timer = new Timer();
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCallLayoutVisible();
        PermissionX.init(OutgoingCall.this)
                .permissions(
                        Manifest.permission.RECORD_AUDIO
                ).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        init();
                    } else {
                        Toast.makeText(OutgoingCall.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                    }
                });
//        binding.switchCameraButton.setVisibility(View.GONE);
        binding.videoButton.setVisibility(View.GONE);
        binding.contactName.setText(getIntent().getStringExtra("contactName"));
        binding.contactNumber.setText(getIntent().getStringExtra("receiverNumber"));

    }
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForegroundService();
            }
        }
    }
    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, AudioCallService.class);
        serviceIntent.putExtra("receiverNumber",receiverNumber);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, AudioCallService.class);
        stopService(serviceIntent);
    }
    public static String removePlusIfPresent(String str) {
        if (str != null && !str.isEmpty() && str.charAt(0) == '+') {
            return str.substring(1);
        }
        return str;
    }

    private String getPublicIP() throws InterruptedException {
        String publicIP = "";
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://api.ipify.org?format=text"); // Using ipify service to get public IP
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000); // Set timeout for connection
            urlConnection.setReadTimeout(5000); // Set timeout for reading input
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                publicIP = response.toString();
            } else {
                // Handle the error response code appropriately
                Log.e(TAG, "Error: Unable to retrieve public IP. Response code: " + responseCode);
            }
        } catch (java.io.FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: URL not found or server not reachable.", e);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return publicIP;
    }


    private String reformatIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }

        String[] segments = ip.split("\\.");
        StringBuilder reformattedIP = new StringBuilder();

        for (String segment : segments) {
            reformattedIP.append(String.format("%03d", Integer.parseInt(segment)));
        }

        return reformattedIP.toString();
    }
    private String publicIP;
    private void init() {
        IntentFilter filter = new IntentFilter("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_CHANGE_SPEAKER");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_ACTION_RESUME");
        registerReceiver(broadcastReceiver, filter);
//        userName = "sip:1001@192.168.0.150";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    publicIP = getPublicIP();
                    publicIP = reformatIP(publicIP);
                    if(callUtilService.getFreeSwitchUserId()==null)    //ChatSDK.shared().getKeyStorage().get("fs_user_id")
                    {
                        websocket.showToast("DID number not found");
                        stopForegroundService();
                        finish();
                    }
                    userName = "sip:"+ callUtilService.getFreeSwitchUserId() +"@103.248.13.73";
                    receiver = removePlusIfPresent(getIntent().getStringExtra("receiverNumber"));
                    String callerNumber = reformatPhoneNumber(callUtilService.getCurrentUserEntityID());     //ChatSDK.auth().getCurrentUserEntityID()
                    if(callerNumber!="")
                    {
                        receiver = "sip:"+publicIP+ callerNumber +receiver+"@103.248.13.73";
                    }
                    else {
                        websocket.showToast("Problem with Caller Number");
                        stopForegroundService();
                        finish();
                    }

                } catch (InterruptedException e) {
                    websocket.showToast("Error");
                    stopForegroundService();
                    finish();
                }
            }
        });

        thread.start();

        try {
            // Wait for the thread to complete or timeout after 5 seconds
            thread.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        if (publicIP == null && publicIP.isEmpty())
//        {
//            finish();
//        }

//        receiver = "sip:1000@192.168.0.150:5080";
//        receiver = "sip:1002@103.248.13.73";
//        userName = getIntent().getStringExtra("username");
        websocket = new Websocket( this,OutgoingCall.this);
        if (userName != null) {
            websocket.initSocket(userName);
        }
        rtcClient = new RTCClient(getApplication(), userName, websocket, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate p0) {
                super.onIceCandidate(p0);
                rtcClient.addIceCandidate(p0);
                if (p0 != null) {
                    String sdpMid = p0.sdpMid;
                    int sdpMLineIndex = p0.sdpMLineIndex;
                    String sdpCandidate = p0.sdp;
                    JanusMessage.Candidate candidate = new JanusMessage.Candidate(sdpCandidate, sdpMid, sdpMLineIndex);
                    JanusMessage candidateMessage = new JanusMessage("trickle", candidate, TID(), sessionId, handleId);
                    try {
                        String message = candidateMessage.toJson(candidateMessage);
                        websocket.sendMessage(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }



            }

            @Override
            public void onAddStream(MediaStream p0) {
                super.onAddStream(p0);
                if (p0 != null && p0.videoTracks.size() > 0) {
                    p0.videoTracks.get(0).addSink(binding.remoteView);
                    Log.d(TAG, "onAddStream: " + p0);
                }
            }
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                super.onIceGatheringChange(iceGatheringState);
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                    // ICE gathering is complete
                    JanusMessage.Candidate candidate = new JanusMessage.Candidate(true);
                    JanusMessage candidateMessage = new JanusMessage("trickle", candidate, TID(), sessionId, handleId);
                    try {
                        String message = candidateMessage.toJson(candidateMessage);
                        websocket.sendMessage(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // If ICE candidates arrive before this event, the complete trickle message will be sent there.
                    // If not, it will be sent when the first ICE candidate arrives.
                }
            }
        });

        rtcAudioManager = new RTCAudioManager(this);
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
//        binding.callBtn.setOnClickListener(v -> {
//        startCall(userName);
        target = receiver;
//        });

//        binding.switchCameraButton.setOnClickListener(v -> rtcClient.switchCamera());

        binding.micButton.setOnClickListener(v -> {
            isMute = !isMute;
            if (isMute) {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            rtcClient.toggleAudio(isMute);
        });

//        binding.videoButton.setOnClickListener(v -> {
//            isCameraPause = !isCameraPause;
//            if (isCameraPause) {
//                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
//            } else {
//                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
//            }
//            rtcClient.toggleCamera(isCameraPause);
//        });

        binding.audioOutputButton.setOnClickListener(v -> {
            isSpeakerMode = !isSpeakerMode;
            if (isSpeakerMode) {
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24);
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24);
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
            }
        });

        binding.endCallButton.setOnClickListener(v -> {
            rtcClient.endCall();
            websocket.stopKeepAliveTimer();
//            setCallLayoutGone();
            hangup();
            stopForegroundService();
            finish();
//            try {
//                Intent intent = new Intent(OutgoingCall.this, Class.forName(getIntent().getStringExtra("activityName")));
//                startActivity(intent);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
        });
    }
    private void setIncomingCallLayoutGone() {
        binding.incomingCallLayout.setVisibility(View.GONE);
    }

    private void setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.setVisibility(View.VISIBLE);
    }

    private void setCallLayoutGone() {
        binding.callLayout.setVisibility(View.GONE);
    }

    private void setCallLayoutVisible() {
        binding.callLayout.setVisibility(View.VISIBLE);
    }

//    private void setWhoToCallLayoutGone() {
//        binding.whoToCallLayout.setVisibility(View.GONE);
//    }

    //    private void setWhoToCallLayoutVisible() {
//        binding.whoToCallLayout.setVisibility(View.VISIBLE);
//    }
    public static String TID()
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
    public void hangup()
    {
        JanusMessage.Body body = new JanusMessage.Body("hangup");
        JanusMessage message = new JanusMessage("message", body, TID(), sessionId, handleId);
        sqLiteCallFragmentHelper = new SQLiteCallFragmentHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteCallFragmentHelper.getWritableDatabase();
        long rowId =  sqLiteCallFragmentHelper.insertData(getIntent().getStringExtra("contactName"), getIntent().getStringExtra("receiverNumber"), getIntent().getStringExtra("photo"));
        if(rowId >0){
//            websocket.showToast("Data Inserted");
//            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
        }
        stopTimer();
        try {
            websocket.sendMessage(message.toJson(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSession() {
        // Construct the JSON message for creating a session
        String createSessionMessage = "{\"janus\":\"create\",\"transaction\":\"" + TID() + "\"}";
        step = 1;
        websocket.sendMessage(createSessionMessage);
    }

    public void attachPlugin(String pluginName) {
        // Construct the JSON message for attaching to a plugin
        String attachMessage = "{\"janus\":\"attach\",\"plugin\":\"" + pluginName + "\",\"opaque_id\":\"" + "siptest-" + TID() + "\",\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + "}";
        websocket.sendMessage(attachMessage);
    }

    public void registerToSIP(String username, String authuser, String displayName, String secret, String proxy) {
        // Construct the JSON message for registering to SIP
        String registerMessage = "{\"janus\":\"message\",\"body\":{\"request\":\"register\",\"username\":\"" + username + "\",\"authuser\":\"" + authuser + "\",\"display_name\":\"" + displayName + "\",\"secret\":\"" + secret + "\",\"proxy\":\"" + proxy + "\"},\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + ",\"handle_id\":" + handleId + "}";
        websocket.sendMessage(registerMessage);
    }

    public void sendKeepAlive() {
        // Construct the JSON message for sending keep alive
        String keepAliveMessage = "{ \"janus\": \"keepalive\", \"session_id\":" + sessionId + ", \"transaction\":\"" + TID() + "\" }";
        websocket.sendMessage(keepAliveMessage);
    }

    public void sendCandidate(String candidate, String sdpMid, int sdpMLineIndex, String transaction) {
        // Construct the JSON message for sending candidate
        String candidateMessage = "{\"janus\":\"trickle\",\"candidate\":{\"candidate\":\"" + candidate + "\",\"sdpMid\":\"" + sdpMid + "\",\"sdpMLineIndex\":" + sdpMLineIndex + "},\"transaction\":\"" + transaction + "\",\"session_id\":" + sessionId + ",\"handle_id\":" + handleId + "}";
        websocket.sendMessage(candidateMessage);
    }

    public void startCall(String receiver) {
        websocket.sendMessage("{ \"janus\": \"message\", \"body\": { \"request\": \"call\", \"uri\": " + receiver + ", \"autoaccept_reinvites\": false }, \"transaction\": " + TID() + ", \"jsep\": { \"type\": \"offer\", \"sdp\": \"v=0\\r\\no=- 804709356943256984 2 IN IP4 127.0.0.1\\r\\ns=-\\r\\nt=0 0\\r\\na=group:BUNDLE 0\\r\\na=extmap-allow-mixed\\r\\na=msid-semantic: WMS de52d32f-97c5-42d2-885a-56207354f5fd\\r\\nm=audio 9 UDP/TLS/RTP/SAVPF 111 63 9 0 8 13 110 126\\r\\nc=IN IP4 0.0.0.0\\r\\na=rtcp:9 IN IP4 0.0.0.0\\r\\na=ice-ufrag:QMDB\\r\\na=ice-pwd:gc12jERQgZdU+uz1tPnQzwRG\\r\\na=ice-options:trickle\\r\\na=fingerprint:sha-256 4B:7C:AE:70:09:F6:06:99:12:78:B2:D0:8E:C6:0C:86:E0:39:B9:5B:0E:69:9E:39:8F:19:4F:A7:55:88:96:6B\\r\\na=setup:actpass\\r\\na=mid:0\\r\\na=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\\r\\na=extmap:2 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\\r\\na=extmap:3 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01\\r\\na=extmap:4 urn:ietf:params:rtp-hdrext:sdes:mid\\r\\na=sendrecv\\r\\na=msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\na=rtcp-mux\\r\\na=rtpmap:111 opus/48000/2\\r\\na=rtcp-fb:111 transport-cc\\r\\na=fmtp:111 minptime=10;useinbandfec=1\\r\\na=rtpmap:63 red/48000/2\\r\\na=fmtp:63 111/111\\r\\na=rtpmap:9 G722/8000\\r\\na=rtpmap:0 PCMU/8000\\r\\na=rtpmap:8 PCMA/8000\\r\\na=rtpmap:13 CN/8000\\r\\na=rtpmap:110 telephone-event/48000\\r\\na=rtpmap:126 telephone-event/8000\\r\\na=ssrc:291203786 cname:hKNnjFzhAYyMftEr\\r\\na=ssrc:291203786 msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\n\" }, \"session_id\": " + sessionId + ", \"handle_id\": " + handleId + " }");
    }

    @Override
    public void handleSentMessage(String message) {
        // Implement logic to handle sent messages
        System.out.println("Sent message: " + message);
    }

    @Override
    public void handleReceivedMessage(String message) {
        // Implement logic to handle received messages
        System.out.println("Received message: " + message);
    }

    @Override
    public void onNewMessage(JanusResponse message) throws JSONException {
        String janusType = message.getJanus();
        switch (janusType) {
            case "keepalive":
                System.out.println("Got a keepalive on session " + sessionId);
                break;
            case "server_info":
            case "success":
                if(message.getSessionId() == 0)
                {
                    JanusResponse.Data = message.getData();
                    sessionId = JanusResponse.Data.getId();
                    attachPlugin("janus.plugin.sip");
                    System.out.println("Got a keepalive on session " + sessionId);
                    websocket.showToast("Janus Connected");
                }
                else
                {
                    JanusResponse.Data = message.getData();
                    handleId = JanusResponse.Data.getId();
//                    registerToSIP(userName, "1001", "1001", "1001", "sip:192.168.0.150:5080");
//                    registerToSIP(userName, "9638000123", "9638000123", "telcobright$9638000123", "sip:103.248.13.73");
                    registerToSIP(userName, callUtilService.getFreeSwitchUserId(), callUtilService.getFreeSwitchUserId(), "telcobright$9638000123", "sip:103.248.13.73");
                    websocket.startKeepAliveTimer();
                }
                System.out.println("Session Running... ");
                break;
            case "timeout":
            {
                System.out.println("Time out....... ");
                websocket.showToast("Time Out");
                websocket.stopKeepAliveTimer();
                websocket.closeSocket();
                stopForegroundService();
                finish();
            }
            break;
            case "event":
                JanusResponse.plugin = message.getPluginData();
                if(JanusResponse.plugin.getData().getResult().getEvent().contains("registering"))
                {
                    System.out.println("Registering...");
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("registered"))
                {
                    System.out.println("Registered");

                    runOnUiThread(() -> {
//                        setWhoToCallLayoutGone();
//                        setCallLayoutVisible();
                        websocket.showToast("Registered Success");
                        rtcClient.startLocalAudio();
                        rtcClient.call(receiver,handleId,sessionId, type);
                    });


                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("calling"))
                {
                    System.out.println("Calling");

                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("ringing"))
                {
                    System.out.println("ringing");
                    websocket.showToast("ringing");
                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("proceeding"))
                {
                    System.out.println("proceeding");
                    websocket.showToast("proceeding");
                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("registration_failed"))
                {

                    websocket.showToast("registration_failed");
                    System.out.println(message.toString());
                    websocket.stopKeepAliveTimer();
                    websocket.closeSocket();
                    stopForegroundService();
                    finish();
                    //some works to do
                }

                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("accepted"))
                {

                    if(message.getJsep().getSdp()!=null) {
                        System.out.println("Got answer SDP");

                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);

                    }
                    else
                    {

                    }
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("progress")
                )
                {
                    if(message.getJsep().getSdp()!=null) {
                        System.out.println("Got answer SDP");

                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);

                    }
                    else
                    {
                        System.out.println("No answer SDP");
                    }
                    //some works to do
                }
                else if (JanusResponse.plugin.getData().getResult().getEvent().contains("updating"))
                {
                    System.out.println("updating");
                    JanusMessage.Jsep = message.getJsep();
                    SessionDescription session = new SessionDescription(
                            SessionDescription.Type.OFFER, message.getJsep().getSdp());
                    rtcClient.onRemoteSessionReceived(session);
                    rtcClient.answer(sessionId, handleId);


                }
                else
                {
                    System.out.println("Some errors occur!");
                }
                break;
            case "webrtcup":

                startTimer();
                if (checkPermissions()) {
                    RTCClientSingleton.getInstance().setRtcClient(rtcClient);
                    startForegroundService();
                } else {
                    requestPermissions();
                }
                binding.micButton.setOnClickListener(v -> {
                    Intent changeNotificationIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE_NOTIFICATION_ICON");
                    callUtilService.getContext().sendBroadcast(changeNotificationIcon);
                    isMute = !isMute;
                    if (isMute) {
                        binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    } else {
                        binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                    }
                    rtcClient.toggleAudio(isMute);
                });
//                System.out.println("webrtcup");
//                websocket.showToast("webrtcup");

                break;

            case "media":
                startTime = System.currentTimeMillis();
                System.out.println("media received");
                break;
            case "hangup":
                System.out.println(message.toString());
                websocket.stopKeepAliveTimer();
                websocket.showToast("hangup");
                websocket.closeSocket();
                rtcClient.stopLocalAudio();
                sqLiteCallFragmentHelper = new SQLiteCallFragmentHelper(this);
                long rowId =  sqLiteCallFragmentHelper.insertData(getIntent().getStringExtra("contactName"), getIntent().getStringExtra("receiverNumber"), getIntent().getStringExtra("photo"));
                if(rowId >0){
//                    websocket.showToast("Data Inserted");
//                    Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
                }
                stopTimer();
                stopForegroundService();
                finish();
//                finishAffinity();
//                handleHangup(json);
                break;
            case "ack":
                System.out.println(message.toString());
                break;
//            case "detached":
//                handleDetached(json);
//                break;

//            case "slowlink":
//                handleSlowLink(json);
//                break;
//            case "error":
//                handleError(json);
//                break;

//            case "timeout":
//                handleTimeout(json);
//                break;
            default:
                System.out.println("Unknown message/event  '" + janusType + "' on session " + sessionId);
                System.out.println(message.toString());
        }
    }

    public  void startTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("Call duration: " + duration / 1000 + " seconds");


                String formattedDuration = formatDuration(duration / 1000);
                runOnUiThread(() -> binding.callDuration.setText(formattedDuration));

            }
        }, 1000, 1000); // Start updating every second
    }

    public  void stopTimer() {
        timer.cancel();
        long duration = System.currentTimeMillis() - startTime;

    }


    private String formatDuration(long durationInSeconds) {
        long hours = durationInSeconds / 3600;
        long minutes = (durationInSeconds % 3600) / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}