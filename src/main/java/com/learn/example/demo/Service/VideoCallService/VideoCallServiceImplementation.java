package com.learn.example.demo.Service.VideoCallService;

import com.learn.example.demo.Constants.iChatApplicationConstants;
import com.learn.example.demo.iChatApplication;
import com.learn.example.demo.Models.VideoCallModels.CallHistory;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Repository.VideoCallRepository.VideoCallRepository;
import com.learn.example.demo.Service.LoginService.LoginServiceImplementation;
import com.learn.example.demo.Utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoCallServiceImplementation implements  VideoCallServiceInterface{
    private static final Logger log = LoggerFactory.getLogger(iChatApplication.class);

    private JwtUtil jwtUtil;
    private ResponseModel response;

    @Autowired
    private LoginServiceImplementation loginServiceImplementation;

    @Autowired
    private LoginFunctionalityRepository loginRepository;

    @Autowired
    private VideoCallRepository repository;

//    @Autowired
    private CallHistory callHistory;

    public VideoCallServiceImplementation(){
        this.response = new ResponseModel();
        this.callHistory = new CallHistory();
        this.jwtUtil = new JwtUtil();
//        this.loginServiceImplementation = new LoginServiceImplementation();
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
            String roomId = sha256(baseString);

            return roomId;
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

    @Override
    public ResponseModel startVideoCall(String userId, String receiverId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, userId)){
                User user = loginRepository.findById(userId).get();
                User receiver = loginRepository.findById(receiverId).get();

                    if (user != null && receiver != null) {
                        if(receiver.getStatus().equalsIgnoreCase("online")) {
                            user.setStatus("online");
                            receiver.setStatus("online");
                            String roomId = generateRoomId(userId);
                            log.info("Room Id Generated!");
                            String userRoomId = user.getRoomId();
                            if (userRoomId.equals("default")) {
                                if (receiver.getRoomId().equals("default")) {
                                    user.setRoomId(roomId);
                                    receiver.setRoomId(roomId);

                                    if (user.getOutgoingCallHistoryId() == null) {
                                        user.setOutgoingCallHistoryId(new ArrayList<>());
                                    }
                                    CallHistory call = new CallHistory();
                                    call.setStartTime(LocalDateTime.now());
                                    call.setCallerId(userId);
                                    call.setReceiverId(receiverId);
                                    call.setRoomId(roomId);
                                    repository.save(call);
                                    List<String> callHistoryList = user.getOutgoingCallHistoryId();
                                    callHistoryList.add(call.getId());
                                    user.setOutgoingCallHistoryId(callHistoryList);
                                    loginRepository.save(user);
                                    if (receiver.getIncomingCallHistoryId() == null) {
                                        receiver.setIncomingCallHistoryId(new ArrayList<>());
                                    }
                                    callHistoryList = receiver.getIncomingCallHistoryId();
                                    callHistoryList.add(call.getId());
                                    receiver.setIncomingCallHistoryId(callHistoryList);
                                    loginRepository.save(receiver);
                                    response.setSuccess(true);
                                    response.setMessage("Video Started!");
                                    log.info("Video Call Started with roomId: " + roomId);
                                } else {
                                    response.setMessage("" + receiver.getUserName() + " is already on a call!");
                                    response.setSuccess(false);
                                    log.info("Id - " + receiverId + " is already on call");
                                }
                            } else {
                                response.setMessage("Cannot conduct call while being on call!");
                                response.setSuccess(false);
                                log.info("Id - " + userId + " is already on call");
                            }
                        }
                        else{
                            response.setMessage("Receiver is Offline!");
                            response.setSuccess(false);
                        }
                    } else {
                        response.setMessage("User doesn't exists!");
                        response.setSuccess(false);
                    }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel endVideoCall(String userId, String receiverId, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, userId)){
                User user = loginRepository.findById(userId).get();
                User receiver = loginRepository.findById(receiverId).get();
                if(user!=null && receiver!=null) {
                    String roomId = user.getRoomId();
                    CallHistory callHistory = repository.findByRoomId(roomId);
                    if(user.getId().equals(callHistory.getCallerId())){
                        // user - outgoing history
                        // receiver - incoming history
                        List<String> callId = user.getOutgoingCallHistoryId();
                        List<CallHistory> callHistoryList = fetchCallHistoryFromId(callId);
                        addEndCallTime(callHistoryList, roomId);
                        user.setOutgoingCallHistoryId(callId);
                        user.setRoomId("default");
                        loginRepository.save(user);

                        callId = receiver.getIncomingCallHistoryId();
                        callHistoryList = fetchCallHistoryFromId(callId);
                        addEndCallTime(callHistoryList, roomId);
                        receiver.setIncomingCallHistoryId(callId);
                        receiver.setRoomId("default");
                        loginRepository.save(receiver);

                        response.setSuccess(true);
                        response.setMessage("Video Call Ended!");
                    }
                    else{
                        // receiver - outgoing history
                        // user - incoming history
                        List<String> callId = receiver.getOutgoingCallHistoryId();
                        List<CallHistory> callHistoryList = fetchCallHistoryFromId(callId);
                        addEndCallTime(callHistoryList, roomId);
                        receiver.setOutgoingCallHistoryId(callId);
                        receiver.setRoomId("default");
                        loginRepository.save(receiver);

                        callId = user.getIncomingCallHistoryId();
                        callHistoryList = fetchCallHistoryFromId(callId);
                        addEndCallTime(callHistoryList, roomId);
                        user.setIncomingCallHistoryId(callId);
                        user.setRoomId("default");
                        loginRepository.save(user);

                        response.setSuccess(true);
                        response.setMessage("Video Call Ended!");
                    }
                    callHistory.setEndTime(LocalDateTime.now());
                    repository.save(callHistory);
                }
                else{
                    response.setMessage("User doesn't exists!");
                    response.setSuccess(false);
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public List<CallHistory> fetchIncomingCallHistory(String userId, String authToken) {
        if(jwtUtil.validateToken(authToken, userId)){
            User user = loginRepository.findById(userId).get();
            List<String> incomingCalls = user.getIncomingCallHistoryId();
            if(incomingCalls==null || incomingCalls.isEmpty()){
                return Collections.emptyList();
            }
            else {
                List<CallHistory> calls = fetchCallHistoryFromId(incomingCalls);
                if(calls!=null) {
                    calls = calls.stream()
                            .sorted(Comparator.comparing(CallHistory::getStartTime).reversed())
                            .collect(Collectors.toList());
                    log.info("Incoming Call History fetched successfully!");
                    return calls;
                }
                else{
                    return Collections.emptyList();
                }
            }
//            List<CallHistory> calls = repository.findByReceiverIdOrderByStartTimeDesc(userId);
        }
        else{
            log.info("Unauthorized Access!");
        }
        return Collections.emptyList();
    }

    @Override
    public String formatTime(String time) {
        try {
            // Define the pattern of your input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy,M,d,H,m,s");

            String[] parts = time.split(",");
            int seconds = Integer.parseInt(parts[5]);
            int microseconds = 0;
            if (parts.length > 6) {
                microseconds = Integer.parseInt(parts[6].substring(0, 3)); // Taking only the first 3 digits for microseconds
            }

            // Create LocalDateTime using extracted values
            LocalDateTime callTime = LocalDateTime.of(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    seconds,
                    microseconds
            );

            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Check if the call time is today
            if (callTime.toLocalDate().equals(currentDate)) {
                String formattedResultTime = "Today at " + callTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                return formattedResultTime;
            }

            // Check if the call time is yesterday
            if (callTime.toLocalDate().equals(currentDate.minusDays(1))) {
                String formattedResultTime = "Yesterday at " + callTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                return formattedResultTime;
            }

            // For any other day, use the default format
//            String formattedResultTime = callTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            String formattedResultTime = callTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            return formattedResultTime;

        } catch (Exception e) {
            System.err.println("Error parsing the input time: " + e.getMessage());
            return "Invalid input time format";
        }
    }



    @Override
    public String computeDuration(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy,M,d,H,m,s,SSSSSSSSS");

        // Parse start and end times
        LocalDateTime s = LocalDateTime.parse(startTime, formatter);
        LocalDateTime e = LocalDateTime.parse(endTime, formatter);

        // Calculate duration
        Duration duration = Duration.between(s, e);

        // Extract hours, minutes, and seconds
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        log.info("Duration Computed !");
        return hours + "-" + minutes + "-" + seconds;
    }


    @Override
    public List<CallHistory> fetchOutgoingCallHistory(String userId, String authToken) {
        if(jwtUtil.validateToken(authToken, userId)){
            User user = loginRepository.findById(userId).get();
            List<String> outgoingCalls = user.getOutgoingCallHistoryId();
            if(outgoingCalls == null || outgoingCalls.isEmpty()){
                return Collections.emptyList();
            }
            else {
                List<CallHistory> calls = fetchCallHistoryFromId(outgoingCalls);
                calls = calls.stream()
                        .sorted(Comparator.comparing(CallHistory::getStartTime).reversed())
                        .collect(Collectors.toList());
                log.info("Outgoing Call History fetched successfully!");
                return calls;
            }
//            List<CallHistory> calls = repository.findByReceiverIdOrderByStartTimeDesc(userId);
        }
        else{
            log.info("Unauthorized Access!");
        }
        return Collections.emptyList();
    }

    @Override
    public ResponseModel clearIncomingCallHistory(String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)) {
                User user = loginRepository.findById(id).get();
                if(user.getIncomingCallHistoryId()!=null) {
                    user.setIncomingCallHistoryId(null);

                    loginRepository.save(user);
                    response.setSuccess(true);
                    response.setMessage("Incoming Call History Cleared Successfully!");
                }
                else{
                    response.setMessage("No History to clear!");
                    response.setSuccess(false);
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");

            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error occured!");
        }
        return response;
    }

    @Override
    public ResponseModel clearOutgoingCallHistory(String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)) {
                User user = loginRepository.findById(id).get();
                if(user.getOutgoingCallHistoryId()!=null){
                    user.setOutgoingCallHistoryId(null);
                    loginRepository.save(user);
                    response.setSuccess(true);
                    response.setMessage("Outgoing Call History Cleared Successfully!");
                }
                else{
                    response.setMessage("No History to clear!");
                    response.setSuccess(false);
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");

            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error occured!");
        }
        return response;
    }

    @Override
    public ResponseModel clearAllHistory(String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)) {
                User user = loginRepository.findById(id).get();
                if(user.getIncomingCallHistoryId()!=null || user.getOutgoingCallHistoryId()!=null) {
                    user.setOutgoingCallHistoryId(null);
                    user.setIncomingCallHistoryId(null);
                    loginRepository.save(user);
                    response.setSuccess(true);
                    response.setMessage("Call History Cleared Successfully!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("Call History Already cleared!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");

            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error occured!");
        }
        return response;
    }

    @Override
    public ResponseModel clearCall(String userId, String callId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, userId)) {
                User user = loginRepository.findById(userId).get();
                if(user.getIncomingCallHistoryId()!=null || user.getOutgoingCallHistoryId()!=null) {
                    if(user.getOutgoingCallHistoryId()!=null) {
                        List<String> callHistory = user.getOutgoingCallHistoryId();
                        if(callHistory.contains((callId))){
                            callHistory.remove(callId);
                        }
                        user.setOutgoingCallHistoryId(callHistory);
                    }
                    if(user.getIncomingCallHistoryId()!=null){
                        List<String> callHistory = user.getIncomingCallHistoryId();
                        if(callHistory.contains((callId))){
                            callHistory.remove(callId);
                        }
                        user.setIncomingCallHistoryId(callHistory);
                    }
                    loginRepository.save(user);
                    response.setSuccess(true);
                    response.setMessage("Call Deleted Successfully!");
                    log.info("Call Deleted Successfully!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("Unauthorized Access!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");

            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error occured!");
        }
        return response;
    }

    private List<CallHistory> fetchCallHistoryFromId(List<String> callId) {
        List<CallHistory> callHistoryList = new ArrayList<>();
        if(callId!=null) {
            Iterator<String> iterator = callId.iterator();
            while(iterator.hasNext()) {
                String id = iterator.next();
                Optional<CallHistory> history = repository.findById(id);
                if(history.isPresent()) {
                    callHistoryList.add(history.get());
                }
                else{
                    iterator.remove();
                }
            }
        }
        return callHistoryList;
    }
    private void addEndCallTime(List<CallHistory> call, String roomId) {
        Iterator<CallHistory> iterator = call.iterator();

        while (iterator.hasNext()) {
            CallHistory history = iterator.next();
            if (history.getRoomId().equals(roomId)) {
                history.setEndTime(LocalDateTime.now());
                repository.save(history);
            }
        }
    }
}
