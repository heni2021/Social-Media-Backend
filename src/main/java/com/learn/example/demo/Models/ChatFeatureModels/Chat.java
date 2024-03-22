package com.learn.example.demo.Models.ChatFeatureModels;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Chats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    private String id;
    private String chatId;
    private String messageId;
    private String senderId;
    private String receiverId;
    private LocalDateTime timeStamp;
    private String content;
    private boolean isEdited=false;
    private boolean isDeletedForever=false;
    private boolean isForwarded = false;
    private List<String> deletedMessageUserId;
}
