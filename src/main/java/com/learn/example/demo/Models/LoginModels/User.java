package com.learn.example.demo.Models.LoginModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Document(collection = "userDetails")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    private String id;
    private String firstName;
    private String emailAddress;
    private String lastName;
    private String password;
    private String userName;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private String tagLine;
    private String status = "Offline";
    private String roomId = "default";
    private String profileLink;
    private byte[] profilePhoto;

    private List<String> followerId;
    private List<String> followingId;
    private List<String> incomingRequestId;
    private List<String> outgoingRequestId;
    private List<String> incomingCallHistoryId;
    private List<String> outgoingCallHistoryId;
    private List<String> postsId;
    private HashMap<String, LocalDateTime> chatId;
}
