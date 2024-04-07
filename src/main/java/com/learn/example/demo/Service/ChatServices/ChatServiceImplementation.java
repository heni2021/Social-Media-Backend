package com.learn.example.demo.Service.ChatServices;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Repository.ChatRepository.ChatFeatureRepository;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ChatServiceImplementation implements ChatServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImplementation.class);

    @Autowired
    private ChatFeatureRepository repository;

    @Autowired
    private LoginFunctionalityRepository loginRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ResponseModel response;

    public ChatServiceImplementation() {
        response = new ResponseModel();
    }
    @Override
    public Chat saveChat(String authToken, Chat chat, String id, String chatId) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                String generatedChatId = generateChatId(chat.getReceiverId(), chat.getSenderId());
                log.info("Chat Id generated successfully - "+chatId);
                if(!chatId.equals(generatedChatId)){
                    log.info("Chat Ids doesn't match!");
                }
                else {
                    String messageId = generateMessageId();
                    chat.setChatId(chatId);
                    chat.setMessageId(messageId);
                    chat.setTimeStamp(LocalDateTime.now());
                    repository.save(chat);
                    log.info("Chat Saved Successfully!");
                    updateUser(id, chatId);
                    updateUser(chat.getReceiverId(), chatId);
                    return chat;
                }
            }
            else{
                log.info("Unauthorized Access for id in saveChat- "+id);
            }
        } catch (Exception e) {
            log.info("Error Occurred! - "+e.getMessage());
        }
        return null;
    }
    @Override
    public ResponseModel deleteAMessageForever(String chatId, String messageId, String id, String authToken) {
        try {
            if (jwtUtil.validateToken(authToken, id)) {
                List<Chat> chats = repository.findAllByChatId(chatId);
                if (chats != null || !chats.isEmpty()) {
                    Chat chat = repository.findByChatIdAndMessageId(chatId, messageId);
                    if (chat != null && chat.getSenderId().equals(id)) {
                        if (isWithin15Minutes(chat.getTimeStamp())) {
                            chat.setDeletedForever(true);
                            chat.setForwarded(false);
                            chat.setEdited(false);
                            chat.setDeletedMessageUserId(new ArrayList<>());
                            chat.setContent("This message is deleted forever!");
                            repository.save(chat);
                            log.info("Message with id - " + messageId + " deleted successfully!");
                            response.setSuccess(true);
                            response.setMessage("Message deleted forever!");
                        } else {
                            log.info(" Message Can't be deleted  after 15 mins");
                            response.setSuccess(false);
                            response.setMessage("Message can't be deleted after 15 mins of its sent time!");
                        }
                    } else {
                        log.info(" Message doesn't exists for id - " + messageId);
                        response.setSuccess(false);
                        response.setMessage("Message Already Deleted! Try Refreshing the page!");
                    }
                } else {
                    log.info(" Chats doesn't exists for id - " + chatId);
                    response.setSuccess(false);
                    response.setMessage("Chats Already Deleted! Try Refreshing the page!");
                }
            } else {
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        } catch (Exception e) {
            log.info("Error Occurred! - " + e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }
    @Override
    public List<Chat> getAllChatsByChatId(String chatId, String id, String authToken) {
        if(jwtUtil.validateToken(authToken,id)){
            Optional<User> users = loginRepository.findById(id);
            if(users.isPresent()){
                User user = users.get();
                HashMap<String, LocalDateTime> chatIds = user.getChatId();
                if(chatIds!=null && chatIds.get(chatId)!=null){
                    chatIds.put(chatId, LocalDateTime.now());
                    user.setChatId(chatIds);
                    loginRepository.save(user);
                    log.info("Last Access Time updated successfully!");
                    List<Chat> chats = repository.findAllByChatIdOrderByTimeStamp(chatId);
                    List<Chat> filteredChats = filterChatsWithDeletedMsgs(chats, id);
                    log.info("Chats Fetched Successfully for id- " + chatId);
                    return filteredChats;
                }
                else{
                    if(chatIds==null){
                        chatIds = new HashMap<>();
                    }
                    chatIds.put(chatId, LocalDateTime.now());
                    user.setChatId(chatIds);
                    loginRepository.save(user);
                    log.info("Empty chat Created!");
                    List<Chat> chats = repository.findAllByChatIdOrderByTimeStamp(chatId);
                    List<Chat> filteredChats = filterChatsWithDeletedMsgs(chats, id);
                    log.info("Chats Fetched Successfully for id- " + chatId);
                    return filteredChats;
                }
            }
        }
        else{
            log.info("Unauthorized Access for id - "+id);
        }
        return Collections.emptyList();
    }



    public String convertDateTime(String inputDateTime) {
        // Parse input date-time string
        LocalDateTime callTime = LocalDateTime.parse(inputDateTime, DateTimeFormatter.ISO_DATE_TIME);

        // Get current date
        LocalDateTime currentDate = LocalDateTime.now();

        // Check if the call time is today
        if (callTime.toLocalDate().equals(currentDate.toLocalDate())) {
            String formattedResultTime = callTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            return formattedResultTime;
        }

        // Check if the call time is yesterday
        if (callTime.toLocalDate().equals(currentDate.minusDays(1).toLocalDate())) {
            String formattedResultTime = "Yesterday at " + callTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            return formattedResultTime;
        }

        // For any other day, use the default format
        String formattedResultTime = callTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        return formattedResultTime;
    }
    @Override
    public ResponseModel canMessageDeleteForever(String chatId, String messageId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                List<Chat> chats = repository.findAllByChatId(chatId);
                if(chats!=null || !chats.isEmpty()){
                    Chat chat = repository.findByChatIdAndMessageId(chatId, messageId);
                    if(chat!=null && chat.getSenderId().equals(id)){
                        if(isWithin15Minutes(chat.getTimeStamp())) {
                            response.setSuccess(true);
                            response.setMessage("This operation can be performed!");
                        }
                        else{
                            log.info(" Message Can't be deleted  after 15 mins");
                            response.setSuccess(false);
                            response.setMessage("This operation can't be performed after 15 mins of its sent time!");
                        }
                    }
                    else{
                        log.info(" Message doesn't exists for id - "+messageId);
                        response.setSuccess(false);
                        response.setMessage("Some Internal Operational Error Occured! Try Refreshing the page!");
                    }
                }
                else{
                    log.info(" Chats doesn't exists for id - "+chatId);
                    response.setSuccess(false);
                    response.setMessage("Chats Already Deleted! Try Refreshing the page!");
                }
            }
            else{
                log.info("Unauthorized Access for id - "+id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }
    @Override
    public ResponseModel editMessage(String chatId, String id, String messageId, String authToken, Chat editedChat) {
        try{
            response = canMessageDeleteForever(chatId, messageId, id, authToken);
            if(response.isSuccess()){
                Chat chat = repository.findByChatIdAndMessageId(chatId, messageId);
                if(!chat.getContent().equals(editedChat.getContent())){
                    chat.setContent(editedChat.getContent());
                    chat.setEdited(true);
                    chat.setTimeStamp(LocalDateTime.now());
                    repository.save(chat);
                    response.setSuccess(true);
                    response.setMessage("Message Edited successfully!");
                    log.info("Message with id - "+messageId+" edited successfully!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("No Content to change");
                    log.info("Message is not edited only!");
                }
            }
            else{
                return response;
            }
        }
        catch (Exception e){
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }

    @Override
    public ResponseModel deleteAMessage(String messageId, String chatId, String id, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)) {
                Chat chat = repository.findByChatIdAndMessageId(chatId, messageId);
                Optional<User> users = loginRepository.findById(id);
                if (users.isPresent()) {
                    if (chat != null) {
                        if (chat.getDeletedMessageUserId() == null) {
                            chat.setDeletedMessageUserId(new ArrayList<>());
                        }
                        List<String> userIDs = chat.getDeletedMessageUserId();
                        if (!userIDs.contains(id)) {
                            userIDs.add(id);
                            chat.setDeletedMessageUserId(userIDs);
                            repository.save(chat);
                        }
                        log.info("Message with id " + messageId + " delivered successfully!");
                        response.setMessage("Message Deleted Successfully!");
                        response.setSuccess(true);
                    } else {
                        log.info("Message with id" + messageId + " Doesn't exists!!");
                        response.setSuccess(false);
                        response.setMessage("Message Doesn't Exists!");
                    }
                } else {
                    log.info("Unauthorized Access for id - " + id);
                    response.setSuccess(false);
                    response.setMessage("Unauthorized Access!");
                }
            }
            else{
                log.info("User with id - "+id+" doesn't exists!");
                response.setSuccess(false);
                response.setMessage("Log out and login again!");
            }
        } catch (Exception e) {
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }

    @Override
    public ResponseModel deleteChat(String chatId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)) {
                Optional<User> optionalUser = loginRepository.findById(id);
                if(optionalUser.isPresent()){
                    User user = optionalUser.get();
                    HashMap<String, LocalDateTime> chatIds = user.getChatId();
                    if(chatIds!=null && chatIds.get(chatId)!=null){
                        chatIds.remove(chatId);
                        user.setChatId(chatIds);
                        loginRepository.save(user);
                        repository.deleteAllByChatId(chatId);
                        response.setMessage("Chats deleted successfully!");
                        response.setSuccess(true);
                        log.info("Chat with chat Id - "+chatId+" deleted successfully!");
                    }
                    else{
                        log.info("Chat Id - "+chatId+" doesn't exists!");
                        response.setSuccess(false);
                        response.setMessage("Some Errors Occured! Try opening page again!");
                    }
                }
                else{
                    log.info("User with id - "+id+" doesn't exists!");
                    response.setSuccess(false);
                    response.setMessage("Log out and login again!");
                }
            }
            else{
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }

    @Override
    public ResponseModel clearChats(String chatId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)) {
                Optional<User> optionalUser = loginRepository.findById(id);
                if(optionalUser.isPresent()){
                    User user = optionalUser.get();
                    HashMap<String, LocalDateTime> chatIds = user.getChatId();
                    if(chatIds!=null && chatIds.get(chatId)!=null){
                        repository.deleteAllByChatId(chatId);
                        response.setMessage("Chats cleared successfully!");
                        response.setSuccess(true);
                        log.info("Chat with chat Id - "+chatId+" cleared successfully!");
                    }
                    else{
                        log.info("Chat Id - "+chatId+" doesn't exists!");
                        response.setSuccess(false);
                        response.setMessage("Some Errors Occured! Try opening page again!");
                    }
                }
                else{
                    log.info("User with id - "+id+" doesn't exists!");
                    response.setSuccess(false);
                    response.setMessage("Log out and login again!");
                }
            }
            else{
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }

    @Override
    public Chat getChatByChatId(String chatId, String msgId) {
        Chat chat = repository.findByChatIdAndMessageId(chatId, msgId);
        return chat;
    }

    @Override
    public ResponseModel forwardMessage(String destinationChatId, String chatId, String msgId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Chat chat = repository.findByChatIdAndMessageId(chatId, msgId);
                if (chat != null  && !chat.isDeletedForever()) {
                    if(!chat.getSenderId().equals(id) && !chat.getChatId().equals(destinationChatId)) {
                        Optional<User> optionalUser = loginRepository.findById(id);
                        log.info("Into other user!!");
                        if(optionalUser.isPresent()) {
                            User user = optionalUser.get();
                            if(user.getChatId()==null){
                                user.setChatId(new HashMap<>());
                            }
                            HashMap<String, LocalDateTime> chatIds = user.getChatId();
                            chatIds.put(destinationChatId, LocalDateTime.now());
                            user.setChatId(chatIds);
                            loginRepository.save(user);
                            Chat forwardedChat = new Chat();
                            String messageId = generateMessageId();
                            forwardedChat.setMessageId(messageId);
                            forwardedChat.setForwarded(true);
                            forwardedChat.setSenderId(id);
                            forwardedChat.setReceiverId(chat.getSenderId());
                            forwardedChat.setChatId(destinationChatId);
                            forwardedChat.setContent(chat.getContent());
                            forwardedChat.setTimeStamp(LocalDateTime.now());
                            repository.save(forwardedChat);
                            response.setSuccess(true);
                            response.setMessage(forwardedChat.getMessageId());
                            log.info("Chat forwarded successfully");
                        }
                        else{
                            log.info("User with id - "+id+" doesn't exists!");
                            response.setSuccess(false);
                            response.setMessage("User doesn't exists!");
                        }
                    }
                    else{
                        if(chat.isForwarded()) {
                            Optional<User> optionalUser = loginRepository.findById(id);
                            if (optionalUser.isPresent()) {
                                User user = optionalUser.get();
                                if (user.getChatId() == null) {
                                    user.setChatId(new HashMap<>());
                                }
                                HashMap<String, LocalDateTime> chatIds = user.getChatId();
                                chatIds.put(destinationChatId, LocalDateTime.now());
                                user.setChatId(chatIds);
                                loginRepository.save(user);
//                                System.out.println(chat.getContent());
                                Chat chats = new Chat();
                                chats.setForwarded(chat.isForwarded());
                                chats.setSenderId(id);
                                String messageId = generateMessageId();
                                chats.setMessageId(messageId);
                                chats.setChatId(destinationChatId);
                                chats.setTimeStamp(LocalDateTime.now());
                                chats.setContent(chat.getContent());
                                chats.setReceiverId(fetchReceiverIdFromChatId(destinationChatId, id));
                                chats.setEdited(chat.isEdited());
                                repository.save(chats);
                                response.setSuccess(true);
                                response.setMessage(chats.getMessageId());
                                log.info("Chat forwarded successfully!!!");
                            } else {
                                log.info("User with id - " + id + " doesn't exists!");
                                response.setSuccess(false);
                                response.setMessage("User doesn't exists!");
                            }
                        }
                        else{
                            Optional<User> optionalUser = loginRepository.findById(id);
                            if (optionalUser.isPresent()) {
                                User user = optionalUser.get();
                                if (user.getChatId() == null) {
                                    user.setChatId(new HashMap<>());
                                }
                                HashMap<String, LocalDateTime> chatIds = user.getChatId();
                                chatIds.put(destinationChatId, LocalDateTime.now());
                                user.setChatId(chatIds);
                                loginRepository.save(user);
//                                System.out.println(chat.getContent());
                                Chat chats = new Chat();
                                chats.setForwarded(false);
                                chats.setSenderId(id);
                                String messageId = generateMessageId();
                                chats.setMessageId(messageId);
                                chats.setChatId(destinationChatId);
                                chats.setTimeStamp(LocalDateTime.now());
                                chats.setContent(chat.getContent());
                                chats.setReceiverId(fetchReceiverIdFromChatId(destinationChatId, id));
                                chats.setEdited(false);
                                repository.save(chats);
                                response.setSuccess(true);
                                response.setMessage(chats.getMessageId());
                                log.info("Chat forwarded successfully!!!");
                            } else {
                                log.info("User with id - " + id + " doesn't exists!");
                                response.setSuccess(false);
                                response.setMessage("User doesn't exists!");
                            }
                        }
                    }
                }
                else{
                    log.info("Chat is with id  - " +msgId+" already deleted!! ");
                    response.setSuccess(false);
                    response.setMessage("Message is Already Deleted!");
                }
            }
            else{
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error Occurred! - "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occurred! Try logging in again!");
        }
        return response;
    }

    @Override
    public ResponseModel countUnreadMsgs(String chatId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> optionalUser = loginRepository.findById(id);
                if(optionalUser.isPresent()){
                    User user = optionalUser.get();
                    HashMap<String, LocalDateTime> chatIds = user.getChatId();
                    if(chatIds==null){
                        chatIds = new HashMap<>();
                        chatIds.put(chatId, LocalDateTime.now());
                        user.setChatId(chatIds);
                        loginRepository.save(user);
                    }

                        LocalDateTime accessTime = chatIds.get(chatId);
                        LocalDateTime currentTime = LocalDateTime.now();
                        if (accessTime != null && accessTime.isBefore(currentTime)) {
                            List<Chat> chats = repository.findAllByChatIdOrderByTimeStamp(chatId);
                            Long unreadChatCount = countUnreadChats(chats, id, accessTime);
                            response.setSuccess(true);
                            response.setMessage(String.valueOf(unreadChatCount));
                        } else {
                            response.setSuccess(true);
                            response.setMessage("0");
                        }
                }
                else{
                    log.info("User Doesn't exists with id- "+id);
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists!! Try relogging!");
                }
            }
            else{
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            log.info("Error Occurred : "+e.getMessage());
            response.setMessage("Some internal error occurred!");
        }
        return response;
    }

    @Override
    public ResponseModel updateAccessTime(String chatId, String id, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> optionalUser = loginRepository.findById(id);
                if(optionalUser.isPresent()){
                    User user = optionalUser.get();
                    HashMap<String, LocalDateTime> chatIds = user.getChatId();
                    chatIds.put(chatId, LocalDateTime.now());
                    user.setChatId(chatIds);
                    loginRepository.save(user);
                    response.setMessage("Time updated successfully!");
                    response.setSuccess(true);
                }
                else{
                    log.info("User Doesn't exists with id- "+id);
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists!! Try relogging!");
                }
            }
            else{
                log.info("Unauthorized Access for id - " + id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            log.info("Error Occurred : "+e.getMessage());
            response.setMessage("Some internal error occurred!");
        }
        return response;
    }

    private Long countUnreadChats(List<Chat> chats, String id, LocalDateTime accessTime) {
        Long count = 0L;
        Iterator<Chat> chatIterator = chats.iterator();
        while(chatIterator.hasNext()){
            Chat chat = chatIterator.next();
            if(chat.getTimeStamp().isAfter(accessTime) && !chat.getSenderId().equals(id)){
                count++;
            }
        }
        return count;
    }

    private List<Chat> filterChatsWithDeletedMsgs(List<Chat> chats, String id) {
        List<Chat> filteredChats = new ArrayList<>();
        Iterator<Chat> chatIterator = chats.iterator();
        while(chatIterator.hasNext()){
            Chat chat = chatIterator.next();
            if(chat.getDeletedMessageUserId()!=null){
                if(chat.getDeletedMessageUserId().contains(id)){
                    chatIterator.remove();
                }
                else{
                    filteredChats.add(chat);
                }
            }
            else{
                filteredChats.add(chat);
            }
        }
        return filteredChats;
    }
    private String generateChatId(String receiverId, String senderId) {
        String[] userIds = {receiverId, senderId};
        Arrays.sort(userIds);
        return userIds[0] + "_" + userIds[1];
    }
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    private void updateUser(String id, String chatId) {
        try{
            Optional<User> userDetail = loginRepository.findById(id);
            if(userDetail.isPresent()){
                User user = userDetail.get();
                if(user.getChatId()==null){
                    user.setChatId(new HashMap<>());
                }

                HashMap<String, LocalDateTime> chatIds = user.getChatId();
                if(chatIds.get(chatId)==null){
                    chatIds.put(chatId, LocalDateTime.now());
                }
                user.setChatId(chatIds);
                loginRepository.save(user);
                log.info("Chats for user saved successfully!");
            }
            else{
                log.info("User doesn't exists with id - "+id);
            }
        }
        catch(Exception e){
            log.info("Error Occurred! - "+e.getMessage());
        }
    }
    public boolean isWithin15Minutes(LocalDateTime timeStamp) {
        // Get current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Calculate the difference in minutes
        long differenceInMinutes = ChronoUnit.MINUTES.between(timeStamp, currentTime);

        // Check if the difference is less than 15 minutes
        return Math.abs(differenceInMinutes) < 15;
    }
    private String fetchReceiverIdFromChatId(String chatId, String id) {
        String receiverId = "";
        String sp[] = chatId.split("_");
        if(sp[0].equals(id)){
            receiverId = sp[1];
        }
        else{
            receiverId = sp[0];
        }
        return receiverId;
    }

}
