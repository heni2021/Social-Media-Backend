package com.learn.example.demo.Models.ConnectionModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "friendRequests")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FriendRequest {
    @Id
    private String id;
    private String senderId;
    private String receiverId;
    private LocalDateTime requestDate;
    private boolean isAccepted;
    private boolean isIgnored;
}
