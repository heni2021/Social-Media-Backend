package com.learn.example.demo.Repository.VideoCallRepository;

import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCallRepository extends MongoRepository<CallHistory, String> {
    CallHistory findByRoomId(String roomId);
    List<CallHistory> findByReceiverIdOrderByStartTimeDesc(String userId);

    List<CallHistory> findByCallerIdOrderByStartTimeDesc(String callerId);

    void deleteAllByCallerId(String id);

    void deleteAllByReceiverId(String id);
}