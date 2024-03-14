package com.learn.example.demo.Repository.ConnectionRepository;

import com.learn.example.demo.Models.ConnectionModels.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionFunctionalityRepository extends MongoRepository<FriendRequest, String> {

    @Query("{'senderId': ?1, 'receiverId': ?0, 'isAccepted': true}")
    FriendRequest findByReceiverIdAndSenderIdAndIsAccepted(String receiverId, String senderId);


    void deleteAllByReceiverId(String receiverId);

    void deleteAllBySenderId(String senderId);

    @Query("{'senderId': ?0, 'receiverId': ?1, 'isIgnored': false}")
    FriendRequest findBySenderIdAndReceiverId(String senderId, String receiverId);

    FriendRequest findByReceiverIdAndSenderId(String receiverId, String id);

//    @Query("{'senderId': ?0, 'receiverId': ?1, 'isAccepted': true}")
//    FriendRequest findBySenderIdAnd(String senderId, String receiverId);
}
