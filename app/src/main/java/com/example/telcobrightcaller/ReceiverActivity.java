package com.example.telcobrightcaller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiverActivity extends AppCompatActivity implements JanusCallHandlerInterface {

    private static Timer timer;
    private static long startTime;
    private Websocket websocket;
    public static long sessionId = 0;
    public long handleId = 0;
    private int step = -1;

    private ActivityCallBinding binding;
    private String userName;
    private String receiver;
    private static String receiverNumber;
    private static String type;
    private RTCClient rtcClient;
    private String TAG = "ReceiverActivityAudio";
    private String target = "";
    private Gson gson = new Gson();
    private boolean isMute = false ;
    private boolean isCameraPause = false;
    private RTCAudioManager rtcAudioManager;
    private boolean isSpeakerMode = false;

    private boolean isVideo = false;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Handler initialCallConnectedHandler;
    private Runnable disconnectConnectedHandler;
    SQLiteCallFragmentHelper sqLiteCallFragmentHelper;
    HashMap<String, Object> newMessage = new HashMap<>();
    CallUtilService callUtilService;

    public static String getType()
    {
        return type;
    }
    public enum CallState {
        IDLE,
        CONNECTING,
        CALLING,
        CONNECTED,
        DISCONNECTED,
        RINGING

        // Add other states as needed
    }
    private static CallState callState;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY".equals(intent.getAction())) {
                callUtilService.removeCallActivity("ReceiverActivity");
                newMessage.put("type", -1);
                runOnUiThread(() -> {
                    callUtilService.sendPushNotification(newMessage);
                    hangup();

                });
                if(type.contains("video")){
                    rtcClient.stopLocalMedia();

                }else {
                    rtcClient.stopLocalAudio();
                }
                rtcClient.endCall();
                finish();
                if (initialCallConnectedHandler != null && disconnectConnectedHandler != null) {
                    initialCallConnectedHandler.removeCallbacks(disconnectConnectedHandler);
                }

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
            else if ("com.codewithkael.webrtcprojectforrecord.ACTION_CONNECTED".equals(intent.getAction())) {
                binding.callStatusId.setText("Connected");
                callState = CallState.CONNECTED;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callUtilService = new TBCallUtilService(getApplicationContext(),"+8801932383889", "+8801932383889");
        callState = CallState.IDLE;

        timer = new Timer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        PermissionX.init(ReceiverActivity.this)
                .permissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                ).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        binding = ActivityCallBinding.inflate(getLayoutInflater());
                        setContentView(binding.getRoot());
                        setCallLayoutVisible();
                        init();
                        callUtilService.addCallActivity("ReceiverActivity",this);
                    } else {
                        Toast.makeText(ReceiverActivity.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                    }
                });
        type = getIntent().getStringExtra("type");
        if(type.equals("audio")){
//            binding.switchCameraButton.setVisibility(View.GONE);
            binding.videoButton.setVisibility(View.GONE);
        }
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

    private void init() {
        callUtilService.mediaStop();
        IntentFilter filter = new IntentFilter("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_CHANGE_SPEAKER");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_ACTION_RESUME");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_CONNECTED");
        registerReceiver(broadcastReceiver, filter);
        receiverNumber = getIntent().getStringExtra("senderNumber");
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(100001);
        userName = callUtilService.getCurrentUserEntityID();

        receiver = getIntent().getStringExtra("senderNumber");
        websocket = new Websocket( this, ReceiverActivity.this);
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
        target = receiver;
//        });

        if(getIntent().getStringExtra("type").equals("audio")){
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
            rtcClient.startLocalAudio();

        }else {
            isSpeakerMode = true;
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);
            binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24);
            rtcClient.initializeSurfaceView(binding.localView);
            rtcClient.initializeSurfaceView(binding.remoteView);
            rtcClient.startLocalVideo(binding.localView);
            binding.videoButton.setOnClickListener(v -> {
                isCameraPause = !isCameraPause;
                if (isCameraPause) {
                    binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                } else {
                    binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
                }
                rtcClient.toggleCamera(isCameraPause);
            });
            binding.remoteView.setOnClickListener(v -> {
                rtcClient.switchCamera();
            });
//            binding.switchCameraButton.setOnClickListener(v -> rtcClient.switchCamera());

        }
        binding.micButton.setOnClickListener(v -> {
            isMute = !isMute;
            if (isMute) {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            rtcClient.toggleAudio(isMute);
        });


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
//            setCallLayoutGone();
            newMessage.put("type",-1);
            callUtilService.sendPushNotification(newMessage);
            if(type.equals("audio"))
            {
                rtcClient.stopLocalAudio();
            }
            else {
                rtcClient.stopLocalMedia();
            }
            websocket.stopKeepAliveTimer();
            hangup();
            stopForegroundService();
            finishAndRemoveTask();

        });



        initialCallConnectedHandler = new Handler(Looper.getMainLooper());

        disconnectConnectedHandler = new Runnable() {
            @Override
            public void run() {
                if (callState != CallState.CONNECTED) {
                    // Get the updated call state ID
                    websocket.showToast("Network Error");
                    callUtilService.removeCallActivity("ReceiverActivity");
                    newMessage.put("type", -1);
                    callUtilService.sendPushNotification(newMessage);
                    rtcClient.stopLocalMedia();
                    rtcClient.endCall();
                    hangup();
                    finish();
                    if (initialCallConnectedHandler != null && disconnectConnectedHandler != null) {
                        initialCallConnectedHandler.removeCallbacks(disconnectConnectedHandler);
                    }

                }
            }
        };
        initialCallConnectedHandler.postDelayed(disconnectConnectedHandler, 20000); // 20 seconds delay
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
                    attachPlugin("janus.plugin.videocall");
//                    websocket.showToast("Janus Connected");
                }
                else
                {
                    JanusResponse.Data = message.getData();
                    handleId = JanusResponse.Data.getId();
//                    registerToSIP(userName, "2001", "2001", "2001", "sip:192.168.0.105:5060");
                    register(userName);
                    websocket.startKeepAliveTimer();
                }
                System.out.println("Session Running... ");
                break;
            case "timeout":
            {
                System.out.println("Time out....... ");
//                websocket.showToast("Time Out");
                websocket.stopKeepAliveTimer();
                websocket.closeSocket();
                stopForegroundService();
                finish();
                if (initialCallConnectedHandler != null && disconnectConnectedHandler != null) {
                    initialCallConnectedHandler.removeCallbacks(disconnectConnectedHandler);
                }

            }
            break;
            case "event":
                JanusResponse.plugin = message.getPluginData();
                if (JanusResponse.plugin.getData().getErrorCode() == 476 || JanusResponse.plugin.getData().getResult().getEvent().contains("registered"))
                {

                    String receiverNumber = getIntent().getStringExtra("senderNumber");
                    String roomName = null;
                    roomName = callUtilService.getCurrentUserEntityID().split("@")[0];

                    String threadEntityID = receiverNumber + "@localhost";
                    String senderId = callUtilService.getCurrentUserEntityID();
                    HashMap<String, HashMap<String, String>> userIds = new HashMap<String, HashMap<String, String>>();
                    HashMap<String, String> users = new HashMap<String, String>();
                    users.put(threadEntityID, receiverNumber);
                    userIds.put("userIds", users);
                    String action = "co.chatsdk.QuickReply";
                    String body = "video call";
                    int callType = -2;
//                    users.put(ThreadId,senderId);
//                    newMessage.put(ThreadId, threadEntityID);
//                    newMessage.put(SenderName, receiverNumber);
//                    newMessage.put(SenderId, senderId);
//                    newMessage.put(UserIds, users);
//                    newMessage.put(Action, action);
//                    newMessage.put(Body, body);
//                    newMessage.put(Type, callType);
                    callUtilService.sendPushNotification(newMessage);

                    System.out.println("Registered");

                    runOnUiThread(() -> {
                        //                        setWhoToCallLayoutGone();
                        //                        setCallLayoutVisible();
//                        websocket.showToast("Registered Success");
                        websocket.showToast("Connecting");
                        //                        rtcClient.startLocalAudio();
                        //                        rtcClient.call(receiver,handleId,sessionId);
                    });


                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("registering"))
                {
                    System.out.println("Registering...");
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
                    if (initialCallConnectedHandler != null && disconnectConnectedHandler != null) {
                        initialCallConnectedHandler.removeCallbacks(disconnectConnectedHandler);
                    }
                    //some works to do
                }

//                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("accepted")

                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("progress")
                )
                {
                    System.out.println("accepted");
                    if(message.getJsep().getSdp()!=null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);

                    }
                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("incomingcall")
                )
                {
                    System.out.println("accepted");
                    if(message.getJsep().getSdp()!=null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.OFFER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);
//                    if(type.equals("audio")){
//                        rtcClient.startLocalAudio();
//                    }else {
//                        rtcClient.initializeSurfaceView(binding.localView);
//                        rtcClient.initializeSurfaceView(binding.remoteView);
//                        rtcClient.startLocalVideo(binding.localView);
//                        binding.videoButton.setOnClickListener(v -> {
//                            isCameraPause = !isCameraPause;
//                            if (isCameraPause) {
//                                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
//                            } else {
//                                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
//                            }
//                            rtcClient.toggleCamera(isCameraPause);
//                        });
//                        binding.switchCameraButton.setOnClickListener(v -> rtcClient.switchCamera());
//
//                    }

                        rtcClient.answer(sessionId, handleId);

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
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("accepted"))
                {
                    System.out.println("accepted");
//                    if(message.getJsep().getSdp()!=null) {
//                        JanusMessage.Jsep = message.getJsep();
//                        SessionDescription session = new SessionDescription(
//                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
//                        rtcClient.onRemoteSessionReceived(session);
//                        runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
//                    }
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
                Intent changeCallAction = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_CONNECTED");
                callUtilService.getContext().sendBroadcast(changeCallAction);

//                websocket.showToast("webrtcup");
                break;
            case "media":
                startTime = System.currentTimeMillis();
                System.out.println("media received");
                break;
            case "hangup":
                System.out.println(message.toString());
//                sqLiteCallFragmentHelper = new SQLiteCallFragmentHelper(this);
//                SQLiteDatabase sqLiteDatabase = sqLiteCallFragmentHelper.getWritableDatabase();
//                long rowId =  sqLiteCallFragmentHelper.insertData(getIntent().getStringExtra("contactName"), getIntent().getStringExtra("receiverNumber"));
//                if(rowId >0){
//                    websocket.showToast("Data Inserted");
////                    Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
//                }
                stopTimer();
                websocket.stopKeepAliveTimer();
//                websocket.showToast("hangup");
                websocket.closeSocket();
                if(type.equals("audio"))
                {
                    rtcClient.stopLocalAudio();
                }
                else {
                    rtcClient.stopLocalMedia();
                }

                rtcClient.endCall();
                stopForegroundService();
                finishAndRemoveTask();
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


    private void setCallLayoutGone() {
        binding.callLayout.setVisibility(View.GONE);
    }

    private void setCallLayoutVisible() {
        binding.callLayout.setVisibility(View.VISIBLE);
    }

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
        String attachMessage = "{\"janus\":\"attach\",\"plugin\":\"" + pluginName + "\",\"opaque_id\":\"" + "videocalltest-" + TID() + "\",\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + "}";
        websocket.sendMessage(attachMessage);
    }

    public void register(String username) {
        // Construct the JSON message for registering to SIP
        String registerMessage = "{\n" +
                "  \"janus\": \"message\",\n" +
                "  \"body\": {\n" +
                "    \"request\": \"register\",\n" +
                "    \"username\": \""+username+"\"\n" +
                "  },\n" +
                " \"transaction\": \"" + TID() + "\",\n" +
                "  \"session_id\": "+sessionId+",\n" +
                "  \"handle_id\": "+handleId+" \n" +
                "}";
        websocket.sendMessage(registerMessage);
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
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Toast.makeText(ReceiverActivity.this, "Call in progress", Toast.LENGTH_SHORT).show();

        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
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
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel the Runnable if it hasn't executed yet
        if (initialCallConnectedHandler != null && disconnectConnectedHandler != null) {
            initialCallConnectedHandler.removeCallbacks(disconnectConnectedHandler);
        }
        stopForegroundService();
        hangup();
    }


}
