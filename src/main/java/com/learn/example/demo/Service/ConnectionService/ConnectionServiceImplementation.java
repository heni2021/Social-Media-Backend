package com.learn.example.demo.Service.ConnectionService;

import com.learn.example.demo.iChatApplication;
import com.learn.example.demo.Models.ConnectionModels.FriendRequest;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Repository.ConnectionRepository.ConnectionFunctionalityRepository;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Service.LoginService.LoginServiceImplementation;
import com.learn.example.demo.Utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConnectionServiceImplementation implements ConnectionServiceInterface{

    private static Logger log = LoggerFactory.getLogger(iChatApplication.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private ResponseModel response;
    private User user, senderUser;

    @Autowired
    private LoginServiceImplementation service;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ConnectionFunctionalityRepository connectionRepository;

    @Autowired
    private LoginFunctionalityRepository loginRepository;


    public ConnectionServiceImplementation(){
        this.response = new ResponseModel();
        this.user = new User();
        this.senderUser = new User();
    }

    @Override
    public ResponseModel sendRequest(String id, String friendId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)) {
                FriendRequest friendRequest = connectionRepository.findBySenderIdAndReceiverId(id, friendId);
                if (friendRequest == null) {
                    FriendRequest friendRequestModel = new FriendRequest();
                    friendRequestModel.setSenderId(id);
                    friendRequestModel.setReceiverId(friendId);
                    friendRequestModel.setRequestDate(LocalDateTime.now());
                    friendRequestModel.setAccepted(false);
                    friendRequestModel.setIgnored(false);

                    connectionRepository.save(friendRequestModel);

                    // add request to friend also
                    User receiver = loginRepository.findById(friendId).get();

                    if (receiver.getIncomingRequestId() == null) {
                        receiver.setIncomingRequestId(new ArrayList<>());
                    }
                    List<String> request = receiver.getIncomingRequestId();
                    request.add(friendRequestModel.getId());
                    receiver.setIncomingRequestId(request);

                    User sender = loginRepository.findById(id).get();
                    if (sender.getOutgoingRequestId() == null) {
                        sender.setOutgoingRequestId(new ArrayList<>());
                    }
                    request = sender.getOutgoingRequestId();
                    request.add(friendRequestModel.getId());
                    sender.setOutgoingRequestId(request);


                    loginRepository.save(receiver);
                    loginRepository.save(sender);

                    response.setSuccess(true);
                    response.setMessage("Request Sent Successfully!");
                    log.info("Friend Request Sent!");
                }
                else {
                    response.setSuccess(false);
                    response.setMessage("Request Already Sent!");
                    log.info("Friend Request Already Sent!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Invalid Token!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Internal Server Error");
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel acceptRequest(String id, String senderId, String authToken) {
        try {
            FriendRequest friendRequestModel = new FriendRequest();
            friendRequestModel = connectionRepository.findBySenderIdAndReceiverId(senderId, id);
            if(jwtUtil.validateToken(authToken, id) && friendRequestModel!=null && !friendRequestModel.isAccepted()){
                user = loginRepository.findById(id).get();
                senderUser = loginRepository.findById(senderId).get();
                if(user!=null && senderUser!=null) {
                    if (senderUser.getFollowingId() == null) {
                        senderUser.setFollowingId(new ArrayList<>());
                    }
                    List<String> friends = senderUser.getFollowingId();
                    friends.add(user.getId());
                    senderUser.setFollowingId(friends);

                    if (user.getFollowerId() == null) {
                        user.setFollowerId(new ArrayList<>());
                    }

                    List<String> followers = user.getFollowerId();
                    followers.add(senderUser.getId());
                    user.setFollowerId(followers);
                    friendRequestModel.setAccepted(true);

                    List<String> friendRequest = user.getIncomingRequestId();
                    friendRequest.remove(friendRequestModel.getId());
                    user.setIncomingRequestId(friendRequest);

                    friendRequest = senderUser.getOutgoingRequestId();
                    friendRequest.remove(friendRequestModel.getId());
                    senderUser.setOutgoingRequestId(friendRequest);


                    loginRepository.save(senderUser);
                    loginRepository.save(user);
                    connectionRepository.save(friendRequestModel);
                    response.setSuccess(true);
                    response.setMessage("Request Accepted Successfully!");
                    log.info("Request Accepted!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User Doesn't Exists");
                    log.info("Either of user is deleted!");
                }
            }
            else{
                if(friendRequestModel.isAccepted()){
                    response.setSuccess(false);
                    response.setMessage("Request Already Accepted!");
                    log.info("Already Accepted Request");
                }
                else {
                    response.setSuccess(false);
                    response.setMessage("Unauthorized Access!");
                    log.info("Access Denied, Unauthorized Access!");
                }
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Internal Server Error");
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public List<User> fetchFollowers(String id) {
        try {
            Optional<User> optionalUser = loginRepository.findById(id);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                log.info("Followers Fetched Successfully!");
                List<String> followerId = user.getFollowerId();
                List<User> friends = fetchUserFromId(followerId);
                user.setFollowerId(followerId);
                loginRepository.save(user);
                return friends;
            } else {
                log.info("User not found with id: " + id);
                return Collections.emptyList(); // Return an empty list instead of null
            }
        } catch (Exception e) {
            log.error("Error fetching followers: " + e.getMessage(), e);
            return Collections.emptyList(); // Return an empty list in case of an error
        }
    }

    private List<User> fetchUserFromId(List<String> ids) {
        List<User> users = new ArrayList<>();
        if(ids!=null) {
            Iterator<String> iterator = ids.iterator();
            while (iterator.hasNext()) {
                String id = iterator.next();
                Optional<User> u = loginRepository.findById(id);
                if (u.isPresent()) {
                    users.add(u.get());
                } else {
                    iterator.remove();
                }
            }
        }
        return users;
    }


    @Override
    public List<User> fetchFollowing(String id) {
        try {
            Optional<User> optionalUser = loginRepository.findById(id);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                log.info("Following Fetched Successfully!");
                List<String> followingId = user.getFollowingId();
                List<User> friends = fetchUserFromId(followingId);
                user.setFollowingId(followingId);
                loginRepository.save(user);
                return friends;
            } else {
                log.info("User not found with id: " + id);
                return Collections.emptyList(); // Return an empty list instead of null
            }
        } catch (Exception e) {
            log.error("Error fetching following list: " + e.getMessage(), e);
            return Collections.emptyList(); // Return an empty list in case of an error
        }
    }

    @Override
    public List<FriendRequest> fetchIncomingRequest(String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = loginRepository.findById(id);
                if(user.isPresent()){
                    log.info("Incoming Request List Fetched Successfully!");
                    List<String> incomingRequestId = user.get().getIncomingRequestId();
                    return fetchFriendListFromId(incomingRequestId);
                }
                else{
                    log.info("User with id "+id+" doesn't exists!");
                    return Collections.emptyList();
                }
            }
            else{
                log.info("Invalid Token : "+authToken);
                return Collections.emptyList();
            }
        }
        catch(Exception e){
            log.error("Error fetching following list: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<FriendRequest> fetchFriendListFromId(List<String> ids) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        if(ids!=null) {
            Iterator<String> iterator = ids.iterator();
            while(iterator.hasNext()) {
                String idValue = iterator.next();
//                for (int i = 0; i < ids.size(); i++) {
                    Optional<FriendRequest> friend = connectionRepository.findById(idValue);
                    if(friend.isPresent()){
                        friendRequests.add(friend.get());
                    }
                    else{
                        iterator.remove();
                    }
//                }
            }

            Collections.sort(friendRequests, Comparator.comparing(FriendRequest::getRequestDate).reversed());

        }
        return friendRequests;
    }

    @Override
    public List<FriendRequest> fetchOutgoingRequest(String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = loginRepository.findById(id);
                if(user.isPresent()){
                    log.info("Outgoing Request List Fetched Successfully!");
                    List<String> outgoingId =  user.get().getOutgoingRequestId();
                    return fetchFriendListFromId(outgoingId);
                }
                else{
                    log.info("User with id "+id+" doesn't exists!");
                    return Collections.emptyList();
                }
            }
            else{
                log.info("Invalid Token : "+authToken);
                return Collections.emptyList();
            }
        }
        catch(Exception e){
            log.error("Error fetching following list: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ResponseModel ignoreRequest(String id, String senderId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                FriendRequest friendRequest = connectionRepository.findBySenderIdAndReceiverId(senderId, id);
                if(friendRequest!=null && !friendRequest.isAccepted() && !friendRequest.isIgnored()){
                    User receiver = loginRepository.findById(id).get();
                    if(receiver!=null) {
                        friendRequest.setIgnored(true);
                        connectionRepository.save(friendRequest);
                        List<String> incomingRequestid = receiver.getIncomingRequestId();
                        incomingRequestid.remove(friendRequest.getId());
                        receiver.setIncomingRequestId(incomingRequestid);
                        loginRepository.save(receiver);
                        response.setSuccess(true);
                        response.setMessage("Request Ignored!");
                        log.info("Request ignored by id - "+id);
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Invalid Receiver!");
                        log.info("Invalid receiver id - " + id);
                    }
                }
                else{
                    if(friendRequest!=null && friendRequest.isAccepted()){
                        response.setSuccess(false);
                        response.setMessage("Request Already Accepted!");
                        log.info("Unable to ignore request as it is accepted by id - " + id);
                    }
                    else if(friendRequest!=null && friendRequest.isIgnored()){
                        response.setSuccess(true);
                        response.setMessage("Request Already Ignored!");
                        log.info("Ignoring the ignored request by id - " + id);
                    }
                    else {
                        response.setSuccess(false);
                        response.setMessage("Request Not Found!");
                        log.info("Request Doesn't exists!");
                    }
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Invalid Token!");
                log.info("Invalid token: "+authToken+" for id "+id);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Internal Server Error");
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel cancelRequest(String id, String receiverId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                FriendRequest friendRequest = connectionRepository.findByReceiverIdAndSenderId(receiverId, id);
                if(friendRequest!=null){
                    User receiver = loginRepository.findById(receiverId).get();
                    User sender = loginRepository.findById(id).get();
                    if(receiver!=null) {
                        List<String> incoming = receiver.getIncomingRequestId();
                        incoming.remove(friendRequest.getId());
                        receiver.setIncomingRequestId(incoming);
                        loginRepository.save(receiver);
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Invalid Receiver!");
                        log.info("Invalid receiver id - "+id);
                    }

                    if(sender!=null){
                        List<String> outgoing = sender.getOutgoingRequestId();
                        outgoing.remove(friendRequest.getId());
                        sender.setOutgoingRequestId(outgoing);
                        loginRepository.save(sender);
                    }

                    connectionRepository.delete(friendRequest);
                    response.setSuccess(true);
                    response.setMessage("Request Cancelled!");
                    log.info("Request Deleted Successfully!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("Request Not Found!");
                    log.info("Request Doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Invalid token: "+authToken+" for id "+id);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Internal Server Error");
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel unfriend(String senderId, String receiverId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, senderId)){
                User sender = loginRepository.findById(senderId).get();
                if(sender!=null){
                    List<String> following = sender.getFollowingId();
                    User receiver = loginRepository.findById(receiverId).get();
                    if(receiver!=null){
                        following.remove(receiver.getId());
                        sender.setFollowingId(following);
                        loginRepository.save(sender);

                        List<String> follower = receiver.getFollowerId();
                        follower.remove(senderId);
                        receiver.setFollowerId(follower);
                        loginRepository.save(receiver);

                        FriendRequest friend = connectionRepository.findBySenderIdAndReceiverId(receiverId,senderId);
                        if(friend!=null)
                        connectionRepository.deleteById(friend.getId());

                        FriendRequest friendRequest = connectionRepository.findByReceiverIdAndSenderIdAndIsAccepted(receiverId, senderId);
                        if(friendRequest!=null)
                        connectionRepository.deleteById(friendRequest.getId());
                        response.setMessage("UnFollowed Successfully!");
                        response.setSuccess(true);
                        log.info(senderId+" unfollowed "+receiverId);
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Invalid Receiver");
                        log.info("Receiver Id : "+senderId+" doesn't exists!");
                    }
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("Invalid User");
                    log.info("Id : "+senderId+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("UnAuthorized Access!");
                log.info("Invalid token: "+authToken+" for id "+senderId);
            }
        }
        catch (Exception e){
            response.setSuccess(false);
            response.setMessage("Internal Server Error");
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public List<User> fetchOtherFriends(String id, String authToken) {
        try {
            if (jwtUtil.validateToken(authToken, id)) {
                List<User> users = service.getAllUsers();
                List<User> unFriendUsers = fetchUnfriendUsers(users, id);
                log.info("Other users fetched successfully!");
                return unFriendUsers;
            } else {
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Id - " + id + " is unauthorized and invalid!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error fetching other friends: " + e.getMessage());
            log.error("Error fetching other friends: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public ResponseModel checkIsFriend(String id, String userId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                User user = loginRepository.findById(id).get();
                if(user!=null){
                    List<String> friends = user.getFollowingId();
                    List<String> outgoingFriendRequest = user.getOutgoingRequestId();
                    List<String> outgoingFriend = fetchUserIdFromOutgoingRequest(outgoingFriendRequest);
                    if((friends!=null && friends.contains(userId)) || (!outgoingFriend.isEmpty() && outgoingFriend.contains(userId))){
//                        System.out.println(userId+" "+true);
                        response.setSuccess(true);
                        response.setMessage("true");
                    }
                    else{
//                        System.out.println(userId+" "+false);
                        response.setSuccess(true);
                        response.setMessage("false");
                    }
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists!");
                    log.info("User doesn't exists with id - "+id);
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Invalid token for id - "+id);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public List searchUser(String pattern) {
        String regexPattern = ".*" + pattern + ".*";

        Criteria criteria = new Criteria().orOperator(
                Criteria.where("userName").regex(regexPattern, "i"),
                Criteria.where("firstName").regex(regexPattern, "i"),
                Criteria.where("lastName").regex(regexPattern, "i")
        );
        log.info("Criteria for query set!!");
        Query query = new Query(criteria);
        log.info("Query executed successfully!");
        return mongoTemplate.find(query, User.class);
    }

    private List<User> fetchUnfriendUsers(List<User> users, String id) {
        Optional<User> u = loginRepository.findById(id);

        if(u.isPresent()) {
            User user = u.get();
            List<String> following = user.getFollowingId();
            List<String> incomingRequest = user.getIncomingRequestId();
            List<String> outcomingRequest = user.getOutgoingRequestId();
            List<String> incomingUserId = fetchUserIdFromIncomingRequest(incomingRequest);
            List<String> outgoingUserID = fetchUserIdFromOutgoingRequest(outcomingRequest);
            Iterator<User> iterator = users.iterator();
            while (iterator.hasNext()) {
                User u1 = iterator.next();
                if ( u1.getId().equals(id) ||
                        (following != null && following.contains(u1.getId())) ||
                        (!incomingUserId.isEmpty() && incomingUserId.contains(u1.getId())) ||
                        (!outgoingUserID.isEmpty() && outgoingUserID.contains(u1.getId()))
                ) {
                    iterator.remove();
                }

            }
            return users;
        }
        else{
            return users;
        }
    }

    private List<String> fetchUserIdFromOutgoingRequest(List<String> outcomingRequest) {
        List<String> id = new ArrayList<>();
        if(outcomingRequest!=null) {
            Iterator<String> iterator = outcomingRequest.iterator();
            while(iterator.hasNext()) {
                String idValue = iterator.next();
                Optional<FriendRequest> request = connectionRepository.findById(idValue);
                if(request.isPresent()) {
                    id.add(request.get().getReceiverId());
                }
                else{
                    iterator.remove();
                }
            }
        }
        return id;
    }

    private List<String> fetchUserIdFromIncomingRequest(List<String> incomingRequest) {
        List<String> id = new ArrayList<>();
        if(incomingRequest!=null) {
            Iterator<String> iterator = incomingRequest.iterator();
            while(iterator.hasNext()) {
                String idValue = iterator.next();
                Optional<FriendRequest> request = connectionRepository.findById(idValue);
                if(request.isPresent()) {
                    id.add(request.get().getSenderId());
                }
                else{
                    iterator.remove();
                }
            }
        }
        return id;
    }
}
