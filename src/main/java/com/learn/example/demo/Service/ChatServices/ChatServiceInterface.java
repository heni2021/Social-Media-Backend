package com.learn.example.demo.Service.ChatServices;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;

import java.util.List;

public interface ChatServiceInterface {
    Chat saveChat(String authToken, Chat chat, String id, String chatId);

    ResponseModel deleteAMessageForever(String chatId, String messageId, String id, String authToken);

    List<Chat> getAllChatsByChatId(String chatId, String id, String authToken);

    String convertDateTime(String time);

    ResponseModel canMessageDeleteForever(String chatId, String messageId, String id, String authToken);

    ResponseModel editMessage(String chatId, String id, String messageId, String authToken, Chat chat);

    ResponseModel deleteAMessage(String messageId, String chatId, String id, String authToken);

    ResponseModel deleteChat(String chatId, String id, String authToken);

    ResponseModel clearChats(String chatId, String id, String authToken);

    Chat getChatByChatId(String chatId, String msgId);

    ResponseModel forwardMessage(String destinationChatId, String chatId, String msgId, String id, String authToken);
}
