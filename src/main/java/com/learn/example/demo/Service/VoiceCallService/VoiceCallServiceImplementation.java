package com.learn.example.demo.Service.VoiceCallService;

import com.learn.example.demo.Constants.iChatApplicationConstants;
import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Models.ResponsesModel.CallsResponseModel;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Repository.VoiceCallRepository.VoiceCallRepository;
import com.learn.example.demo.Utility.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VoiceCallServiceImplementation implements VoiceCallServiceInterface {
    private CallsResponseModel response;
    private ResponseModel responseModel;
    private JwtUtil jwtUtil;

    @Autowired
    private VoiceCallRepository repository;

    @Autowired
    private LoginFunctionalityRepository loginRepository;

    public VoiceCallServiceImplementation() {
        response = new CallsResponseModel();
        jwtUtil = new JwtUtil();
        responseModel = new ResponseModel();
    }
    @Override
    public CallsResponseModel startVoiceCall(String userId, String receiverId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, userId)){
                Optional<User> optionalUserId = loginRepository.findById(userId);
                Optional<User> optionalReceiverId = loginRepository.findById(receiverId);
                if(optionalUserId.isPresent()){
                    User user = optionalUserId.get();
                    if(optionalReceiverId.isPresent()){
                        User receiver = optionalReceiverId.get();
                        if(receiver.getRoomId().equals("default")) {
                            String roomId = generateRoomId(generateId(userId, receiverId));
                            CallHistory call = new CallHistory();
                            addCallHistoryDetails(userId, receiverId, call, roomId);
                            updateUserDetailsBasedOnCall(user, receiver, call, roomId);
                            response.setSuccess(true);
                            response.setMessage("Calling");
                            response.setRoomId(roomId);
                            response.setCallerId(userId);
                            response.setReceiverId(receiverId);
                            log.info("User with id-" +userId+" is calling - "+receiverId);
                        }
                        else{
                            response.setSuccess(false);
                            response.setMessage("User is busy!");
                            log.info("Receiver with id - "+ receiverId + " is on call!");
                        }
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Receiver doesn't exists!");
                        log.info("Receiver with id - "+ receiverId + " doesn't exists!");
                    }                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists");
                    log.info("User with id - "+userId+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Unauthorized Access with id - "+userId);
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred!!");
            log.info("Error : "+e.getMessage());
        }
        return response;
    }

    @Override
    public CallsResponseModel answerVoiceCall(String receiverId, String roomId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, receiverId)){
                Optional<User> optionalReceiverId = loginRepository.findById(receiverId);
                if(optionalReceiverId.isPresent()){
                    User receiver = optionalReceiverId.get();
                    if(receiver.getRoomId().equals(roomId)){
                        CallHistory call = repository.findByRoomId(roomId);
                        call.setAnswered(true);
                        call.setStartTime(LocalDateTime.now());
                        repository.save(call);
                        response.setSuccess(true);
                        response.setMessage("Answering");
                        response.setRoomId(roomId);
                        response.setReceiverId(call.getReceiverId());
                        response.setCallerId(call.getCallerId());
                        log.info("Answering call...");
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Room Id is not valid!!");
                        log.info("Room id is not valid - "+roomId);
                    }
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Unauthorized Access with id - "+receiverId);
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred!!");
            log.info("Error : "+e.getMessage());
        }
        return response;
    }

    @Override
    public CallsResponseModel endVoiceCall(String userId, String receiverId, String roomId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, userId)){
                Optional<User> optionalUserId = loginRepository.findById(userId);
                Optional<User> optionalReceiverId = loginRepository.findById(receiverId);
                if(optionalUserId.isPresent()){
                    User user = optionalUserId.get();
                    if(optionalReceiverId.isPresent()){
                        User receiver = optionalReceiverId.get();
                        if(user.getRoomId().equals(roomId) && receiver.getRoomId().equals(roomId)){
                            user.setRoomId("default");
                            receiver.setRoomId("default");
                            loginRepository.save(user);
                            loginRepository.save(receiver);

                            CallHistory call = repository.findByRoomId(roomId);
                            call.setEndTime(LocalDateTime.now());
                            repository.save(call);
                            response.setSuccess(true);
                            response.setMessage("Ended");
                        }
                        else{
                            response.setSuccess(false);
                            response.setMessage("Call is not ongoing!!");
                            log.info("Call with roomId doesn't exists! "+roomId);
                        }
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Receiver doesn't exists!");
                        log.info("Receiver with id - "+ receiverId + " doesn't exists!");
                    }                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists");
                    log.info("User with id - "+userId+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Unauthorized Access with id - "+userId);
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred!!");
            log.info("Error : "+e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel sendMessage(String userId, String roomId, String authToken, Chat chat) {
        try{
            if(jwtUtil.validateToken(authToken, userId)){
                Optional<User> optionalUser = loginRepository.findById(userId);
                if(optionalUser.isPresent()){
                    User user = optionalUser.get();
                    if(user.getRoomId().equals(roomId)){
                        chat.setId(generateIdForChat(userId,roomId ));
                        chat.setTimeStamp(LocalDateTime.now());
                        responseModel.setSuccess(true);
                        responseModel.setMessage("Message sent successfully");
                    }
                    else{
                        responseModel.setSuccess(false);
                        responseModel.setMessage("User is not on call!!!");
                        log.info("Invalid room id - "+roomId);
                    }
                }
                else{
                    responseModel.setSuccess(false);
                    responseModel.setMessage("User doesn't exists");
                    log.info("User with id - "+userId+" doesn't exists!");
                }
            }
            else{
                responseModel.setSuccess(false);
                responseModel.setMessage("Unauthorized Access!");
                log.info("Unauthorized Access with id - "+userId);
            }
        }
        catch(Exception e){
            responseModel.setSuccess(false);
            responseModel.setMessage("Internal Error Occurred!!");
            log.info("Error : "+e.getMessage());
        }
        return responseModel;
    }

    private String generateIdForChat(String userId, String roomId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = userId + "_" + roomId + "_" + timestamp;
        return uniqueId;
    }

    private void updateUserDetailsBasedOnCall(User user, User receiver, CallHistory call, String roomId) {
        List<String> outgoingCallHistoryId = user.getOutgoingCallHistoryId();
        List<String> incomingCallHistoryId = receiver.getIncomingCallHistoryId();
        if (outgoingCallHistoryId == null) {
            user.setOutgoingCallHistoryId(new ArrayList<>());
        }
        outgoingCallHistoryId = user.getOutgoingCallHistoryId();

        if (incomingCallHistoryId == null) {
            receiver.setIncomingCallHistoryId(new ArrayList<>());
        }
        incomingCallHistoryId = receiver.getIncomingCallHistoryId();


        outgoingCallHistoryId.add(call.getId());
        incomingCallHistoryId.add(call.getId());
        user.setRoomId(roomId);
        receiver.setRoomId(roomId);
        loginRepository.save(user);
        loginRepository.save(receiver);
    }

    private void addCallHistoryDetails(String userId, String receiverId, CallHistory call, String roomId) {
        call.setVoiceCall(true);
        call.setStartTime(LocalDateTime.now());
        call.setCallerId(userId);
        call.setReceiverId(receiverId);
        call.setRoomId(roomId);
        repository.save(call);
    }

    private String generateId(String userId, String receiverId) {
        String users[] = {userId, receiverId};
        Arrays.sort(users);
        return users[0]+"_"+users[1];
    }

    public String generateRoomId(String userId) {
        try {
            // Use current timestamp as a base for uniqueness
            long timestamp = Instant.now().toEpochMilli();

            // Use a secure random number for additional randomness
            SecureRandom secureRandom = new SecureRandom();
            int randomInt = secureRandom.nextInt();

            // Combine timestamp and random number to form the basis of the RoomId
            String baseString = iChatApplicationConstants.SECRET_KEY_ROOM_ID + timestamp + randomInt + userId;

            // Use a secure hash function to generate a fixed-size unique identifier

            return sha256(baseString);
        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException, e.g., log the error
            e.printStackTrace();
            return null;
        }
    }

    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
