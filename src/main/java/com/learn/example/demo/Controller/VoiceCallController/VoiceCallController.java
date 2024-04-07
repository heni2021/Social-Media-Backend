package com.learn.example.demo.Controller.VoiceCallController;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.ResponsesModel.CallsResponseModel;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Service.VoiceCallService.VoiceCallServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class VoiceCallController {
    @Autowired
    private VoiceCallServiceImplementation service;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/start/voice/call/{userId}/{receiverId}")
    public CallsResponseModel startVoiceCall(@PathVariable String userId, @PathVariable String receiverId, @RequestHeader("auth-token") String authToken){
        CallsResponseModel response = service.startVoiceCall(userId, receiverId, authToken);
        if(response.isSuccess()){
            messagingTemplate.convertAndSend("/topic/voice-call-subscribe/"+receiverId, response);
        }
        return response;
    }

    @PutMapping("/answer/voice/call/{receiverId}/{roomId}")
    public CallsResponseModel answerVoiceCall(@PathVariable String receiverId, @PathVariable String roomId, @RequestHeader("auth-token") String authToken){
        CallsResponseModel response = service.answerVoiceCall(receiverId, roomId, authToken);
        if(response.isSuccess()){
            messagingTemplate.convertAndSend("/topic/voice-call-subscribe/"+response.getReceiverId(), response);
            messagingTemplate.convertAndSend("/topic/voice-call-subscribe/"+response.getCallerId(), response);
        }
        return response;
    }

    @PostMapping("/end/voice/call/{userId}/{receiverId}/{roomId}")
    public CallsResponseModel endVoiceCall(@PathVariable String userId, @PathVariable String receiverId, @PathVariable String roomId, @RequestHeader("auth-token") String authToken){
        CallsResponseModel response = service.endVoiceCall(userId, receiverId, roomId, authToken);
        if(response.isSuccess()){
            messagingTemplate.convertAndSend("/topic/voice-call-subscribe/"+receiverId, response);
            messagingTemplate.convertAndSend("/topic/voice-call-subscribe/"+userId, response);
        }
        return  response;
    }

    @PostMapping("/send/{userId}/{roomId}")
    public ResponseModel sendChatMessage(@PathVariable String userId, @PathVariable String roomId, @RequestHeader("auth-token") String authToken, @RequestBody Chat chat){
        ResponseModel response = service.sendMessage(userId, roomId, authToken, chat);
        if(response.isSuccess()){
            messagingTemplate.convertAndSend("/topic/voice/chat/"+roomId, chat);
        }
        return response;
    }

}
