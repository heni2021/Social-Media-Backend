package com.learn.example.demo.Controller.ConnectionsController;

import com.learn.example.demo.Models.ConnectionModels.FriendRequest;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Service.ConnectionService.ConnectionServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class ConnectionsFunctionalityController {
    @Autowired
    private ConnectionServiceImplementation serviceImplementation;

    @PostMapping("/sendRequest/{id}/{friendId}")
    public ResponseModel sendFriendRequest(@PathVariable String id, @PathVariable String friendId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.sendRequest(id, friendId, authToken);
    }

    @PostMapping("/acceptRequest/{id}/{senderId}")
    public ResponseModel acceptFriendRequest(@PathVariable String id, @PathVariable String senderId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.acceptRequest(id, senderId, authToken);
    }

    @GetMapping("/fetchFollowers/{id}")
    public List<User> fetchFollowers(@PathVariable String id){
        return serviceImplementation.fetchFollowers(id);
    }

    @GetMapping("/fetchFollowing/{id}")
    public List<User> fetchFollowing(@PathVariable String id){
        return serviceImplementation.fetchFollowing(id);
    }

    @GetMapping("/fetchIncomingRequest/{id}")
    public List<FriendRequest> fetchIncomingRequest(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchIncomingRequest(id, authToken);
    }

    @GetMapping("/fetchOutgoingRequest/{id}")
    public List<FriendRequest> fetchOutgoingRequest(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchOutgoingRequest(id, authToken);
    }

    @PostMapping("/ignoreRequest/{id}/{senderId}")
    public ResponseModel ignoreRequest(@PathVariable String id, @PathVariable String senderId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.ignoreRequest(id, senderId, authToken);
    }

    @DeleteMapping("/cancelRequest/{id}/{receiverId}")
    public ResponseModel cancelRequest(@PathVariable String id, @PathVariable String receiverId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.cancelRequest(id, receiverId, authToken);
    }

    @PostMapping("/unfriend/{id}/{receiverId}")
    public ResponseModel unfriend(@PathVariable String id, @PathVariable String receiverId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.unfriend(id, receiverId, authToken);
    }
    @GetMapping("/isfriend/{id}/{userId}")
    public ResponseModel isFriend(@PathVariable String id, @PathVariable String userId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.checkIsFriend(id, userId, authToken);
    }

    @GetMapping("/fetchOtherFriends/{id}")
    public List<User> fetchOtherFriends(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchOtherFriends(id, authToken);
    }

    @GetMapping("/search/{pattern}")
    public List<User> searchUser(@PathVariable String pattern){
        return serviceImplementation.searchUser(pattern);
    }
}