package com.learn.example.demo.Models.ResponsesModel;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CallsResponseModel {
    private boolean isSuccess;
    private String callerId;
    private String receiverId;
    private String roomId;
    private String message;
}
