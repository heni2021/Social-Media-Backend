package com.learn.example.demo.Repository.VoiceCallRepository;

import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceCallRepository extends MongoRepository<CallHistory, String> {
    CallHistory findByRoomId(String roomId);
}
