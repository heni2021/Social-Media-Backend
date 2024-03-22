package com.learn.example.demo.Controller.ChatController;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Service.ChatServices.ChatServiceImplementation;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
public class ChatController {
    private static final Logger log= LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatServiceImplementation service;

    @Autowired
    private SimpMessagingTemplate  messagingTemplate;

    @MessageMapping("/send/{id}/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public Chat saveMessage( @DestinationVariable String id, @DestinationVariable String chatId, @Header("auth-token") String authToken, @Payload Chat chat){
        log.info("Chat Message Fetched!");
        return service.saveChat(authToken, chat, id, chatId);
    }

    @DeleteMapping("/chat/{chatId}/message/{messageId}/forever/{id}")
    public ResponseModel deleteAMessageForever(@PathVariable String chatId, @PathVariable String messageId, @PathVariable String id, @RequestHeader("auth-token") String authToken) {
        ResponseModel response = service.deleteAMessageForever(chatId, messageId, id, authToken);
        if (response.isSuccess()) {
            Chat deletedChat = service.getChatByChatId(chatId, messageId);
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, deletedChat);
        }
        return response;
    }

    @GetMapping("/canDeleteForever/{chatId}/{messageId}/{id}")
    public ResponseModel canDeleteMessageForever(@PathVariable String chatId, @PathVariable String messageId, @PathVariable String id,@RequestHeader("auth-token") String authToken){
        return service.canMessageDeleteForever(chatId, messageId, id, authToken);
    }

    @GetMapping("/chat/getAllChats/{chatId}/{id}")
    public List<Chat> getAllChatsByChatId(@PathVariable String chatId, @PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return service.getAllChatsByChatId(chatId, id, authToken);
    }

    @PutMapping("/edit/chat/{chatId}/{id}/{messageId}")
    public ResponseModel editChatMessage(@PathVariable String chatId, @PathVariable String id, @PathVariable String messageId, @RequestHeader("auth-token") String authToken, @RequestBody Chat chat){
        ResponseModel responseModel =  service.editMessage(chatId, id, messageId, authToken, chat);
        if(responseModel.isSuccess()){
            Chat editedChat = service.getChatByChatId(chatId, messageId);
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, editedChat);
        }
        return responseModel;
    }

    @PutMapping("/forward/{destinationChatId}/{chatId}/{msgId}/{id}")
    public ResponseModel forwardMsg(@PathVariable String destinationChatId, @PathVariable String chatId, @PathVariable String msgId, @PathVariable String id, @RequestHeader("auth-token") String authToken){
        log.info("Hit forward msg!");
        ResponseModel response = service.forwardMessage(destinationChatId, chatId, msgId, id, authToken);
        if(response.isSuccess()){
            Chat forwardedChat = service.getChatByChatId(destinationChatId, response.getMessage());
            messagingTemplate.convertAndSend("/topic/chat/" + destinationChatId, forwardedChat);
        }
        return response;
    }

    @GetMapping("/convert/{time}")
    public String convertTime(@PathVariable String time){
        return service.convertDateTime(time);
    }

    @DeleteMapping("/delete/message/{messageId}/{chatId}/{id}")
    public ResponseModel deleteAMessage(@PathVariable String messageId, @PathVariable String chatId, @PathVariable String id, @RequestHeader("auth-token") String authToken){
        return service.deleteAMessage(messageId, chatId, id, authToken);
    }

    @DeleteMapping("/deleteChats/{chatId}/{id}")
    public ResponseModel deleteChats(@PathVariable String chatId, @PathVariable String id, @RequestHeader("auth-token") String authToken){
        return service.deleteChat(chatId, id, authToken);
    }

    @DeleteMapping("/clearChats/{chatId}/{id}")
    public ResponseModel clearChats(@PathVariable String chatId, @PathVariable String id, @RequestHeader("auth-token") String authToken){
        return service.clearChats(chatId, id, authToken);
    }
}