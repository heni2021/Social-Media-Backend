package com.learn.example.demo.Service.VideoCallService;

import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;

import java.util.List;

public interface VideoCallServiceInterface {
    ResponseModel startVideoCall(String userId, String receiverId, String authToken);

    ResponseModel endVideoCall(String userId, String receiverId, String authToken);

    List<CallHistory> fetchIncomingCallHistory(String userId, String authToken);

    String formatTime(String time);

    String computeDuration(String startTime, String endTime);

    List<CallHistory> fetchOutgoingCallHistory(String userId, String authToken);

    ResponseModel clearIncomingCallHistory(String id, String authToken);

    ResponseModel clearOutgoingCallHistory(String id, String authToken);

    ResponseModel clearAllHistory(String id, String authToken);

    ResponseModel clearCall(String userId, String callId, String authToken);
}
