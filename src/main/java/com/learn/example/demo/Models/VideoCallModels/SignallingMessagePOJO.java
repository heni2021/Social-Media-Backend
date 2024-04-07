package com.learn.example.demo.Models.VideoCallModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class SignallingMessagePOJO {
    private String sender;
    private String receiver;
    private String type;
    private Object data;
}
