package com.learn.example.demo.Models.VideoCallModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "CallHistory")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CallHistory {
    @Id
    private String id;
    private String callerId;
    private String receiverId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomId;
    private boolean isVoiceCall=false;
    private boolean isAnswered=false;
}