package com.learn.example.demo.Controller.VideoCallController;

import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import com.learn.example.demo.Service.VideoCallService.VideoCallServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-call")
public class VideoCallController {

    @Autowired
    private VideoCallServiceImplementation serviceImplementation;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/startCall/{userId}/{receiverId}/{voiceCall}")
    public ResponseModel startVideoCall(@PathVariable String userId, @PathVariable String receiverId, @PathVariable String voiceCall, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        ResponseModel responseModel= serviceImplementation.startVideoCall(userId, receiverId, authToken, voiceCall);
        if(responseModel.isSuccess()){
            messagingTemplate.convertAndSend("/topic/video-call-subscribe/"+receiverId, userId+"_"+responseModel.getMessage());
            messagingTemplate.convertAndSend("/topic/video-call-started/"+responseModel.getMessage(), responseModel);
        }
        return responseModel;
    }

    @PutMapping("/answerCall/{receiverId}")
    public ResponseModel answerVideoCall(@PathVariable String receiverId, @RequestParam String roomId, @RequestHeader("auth-token") String authToken){
        ResponseModel responseModel = serviceImplementation.answerCall(receiverId, roomId, authToken);
        if(responseModel.isSuccess()){
            messagingTemplate.convertAndSend("/topic/video-call-started/"+roomId, responseModel);
        }
        return responseModel;
    }

    @PostMapping("/endCall/{userId}/{receiverId}")
    public ResponseModel endVideoCall(@PathVariable String userId, @PathVariable String receiverId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        ResponseModel responseModel= serviceImplementation.endVideoCall(userId, receiverId, authToken);
        if(responseModel.isSuccess()){
            messagingTemplate.convertAndSend("/topic/video-call-subscribe/"+receiverId, "ended");
            messagingTemplate.convertAndSend("/topic/video-call-subscribe/"+userId, "ended");
        }
        return responseModel;
    }

    @GetMapping("/getHistory/incoming/{userId}")
    public List<CallHistory> getIncomingCallHistory(@PathVariable String userId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchIncomingCallHistory(userId, authToken);
    }

    @GetMapping("/getHistory/outgoing/{userId}")
    public List<CallHistory> getOutgoingCallHistory(@PathVariable String userId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchOutgoingCallHistory(userId, authToken);
    }

    @GetMapping("/formatTime/{time}")
    public String formatTime(@PathVariable String time){
        return serviceImplementation.formatTime(time);
    }

    @GetMapping("/findDuration/{startTime}/{endTime}")
    public String findDuration(@PathVariable String startTime, @PathVariable String endTime){
        return serviceImplementation.computeDuration(startTime, endTime);
    }

    @DeleteMapping("/clearIncomingHistory/{id}")
    public ResponseModel clearIncomingCalls(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.clearIncomingCallHistory(id, authToken);
    }

    @DeleteMapping("/clearOutgoingHistory/{id}")
    public ResponseModel clearOutgoingHistory(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.clearOutgoingCallHistory(id, authToken);
    }

    @DeleteMapping("/clearAll/{id}")
    public ResponseModel clearAllCallHistory(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.clearAllHistory(id, authToken);
    }

    @DeleteMapping("/deleteCall/{userId}/{callId}")
    public ResponseModel deleteCallHistory(@PathVariable String userId, @PathVariable String callId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.clearCall(userId, callId, authToken);
    }
}
