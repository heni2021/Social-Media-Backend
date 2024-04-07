package com.learn.example.demo.Controller.VoiceCallController;

import com.learn.example.demo.Models.VideoCallModels.SignallingMessagePOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignallingMessageController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendSignallingMessage(@Payload SignallingMessagePOJO message){
        messagingTemplate.convertAndSendToUser(message.getReceiver(), "/topic/signaling", message);
    }
}
