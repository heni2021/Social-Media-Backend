package com.learn.example.demo.Service.VoiceCallService;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.ResponsesModel.CallsResponseModel;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;

public interface VoiceCallServiceInterface {
    CallsResponseModel startVoiceCall(String userId, String receiverId, String authToken);

    CallsResponseModel answerVoiceCall(String receiverId, String roomId, String authToken);

    CallsResponseModel endVoiceCall(String userId, String receiverId, String roomId, String authToken);

    ResponseModel sendMessage(String userId, String roomId, String authToken, Chat chat);
}
