package com.learn.example.demo.Service.ConnectionService;

import com.learn.example.demo.Models.ConnectionModels.FriendRequest;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.LoginModels.User;

import java.util.List;

public interface ConnectionServiceInterface {
    ResponseModel sendRequest(String id, String friendId, String authToken);

    ResponseModel acceptRequest(String id, String senderId, String authToken);
    
    List<User> fetchFollowers(String id);

    List<User> fetchFollowing(String id);

    List<FriendRequest> fetchIncomingRequest(String id, String authToken);

    List<FriendRequest> fetchOutgoingRequest(String id, String authToken);

    ResponseModel ignoreRequest(String id, String senderId, String authToken);

    ResponseModel cancelRequest(String id, String receiverId, String authToken);

    ResponseModel unfriend(String id, String receiverId, String authToken);

    List<User> fetchOtherFriends(String id, String authToken);

    ResponseModel checkIsFriend(String id, String userId, String authToken);

    List<User> searchUser(String pattern);
}
