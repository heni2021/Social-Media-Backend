package com.learn.example.demo.Service.LoginService;

import com.learn.example.demo.Constants.iChatApplicationConstants;
import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Repository.ChatRepository.ChatFeatureRepository;
import com.learn.example.demo.Repository.ConnectionRepository.ConnectionFunctionalityRepository;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Repository.PostsRepository.PostsRepository;
import com.learn.example.demo.Repository.VideoCallRepository.VideoCallRepository;
import com.learn.example.demo.Utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LoginServiceImplementation implements LoginServiceInterface {

//    @Autowired
//    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private LoginFunctionalityRepository repository;

    @Autowired
    private ConnectionFunctionalityRepository connectionRepo;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private VideoCallRepository callRepository;

    @Autowired
    private ChatFeatureRepository chatRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private iChatApplicationConstants appConstants;
    private ResponseModel response;

    private static final Logger log = LoggerFactory.getLogger(LoginServiceImplementation.class);

    public LoginServiceImplementation() {
        response = new ResponseModel();
    }

    @Override
    public ResponseModel createUser(User user) {
        try {
            String message = "";
            if (!(user.getEmailAddress().isEmpty()|| user.getFirstName().isEmpty() || user.getLastName().isEmpty() || user.getPassword().isEmpty())) {
                if ((isValidEmail(user.getEmailAddress())) && !ExistsEmail(user.getEmailAddress()) && (isValidPassword(user.getPassword())) && user.getFirstName().length() >= 3 && user.getLastName().length() >= 3) {
                    String firstName = user.getFirstName();
                    String lastName = user.getLastName();
                    if (user.getUserName().isEmpty()) {
                        user.setUserName(firstName.substring(0, 2) + lastName.substring(lastName.length() - 2, lastName.length()));
                    }
                    if(user.getTagLine().isEmpty()){
                        user.setTagLine("Hey there! I am using iChat Application!");
                    }
                    String hashedPassword = passwordEncoder.encode(user.getPassword()+ iChatApplicationConstants.HASH_SALT);
                    user.setPassword(hashedPassword);
                    user.setStatus("online");
                    String link = "https://profile:open/"+user.getUserName()+"/"+generateUniquePostLink(user.getEmailAddress());
                    user.setProfileLink(link);
                    repository.save(user);
                    log.info("Generating Token!");
                    String authToken = generateToken(user);
                    log.info("User Created Successfully!");
                    response.setSuccess(true);
                    response.setMessage(authToken);
                } else {
                    response.setSuccess(false);
                    if (!isValidEmail(user.getEmailAddress())) {
                        message = "Enter a valid email!";
                    } else if(ExistsEmail(user.getEmailAddress())){
                        message="This email id is already registered!";
                    }else if (!isValidPassword(user.getPassword())) {
                        message = "Enter a strong password with at least " +
                                "one upper case," +
                                "one lower case, and " +
                                "one special character - (#,@,$)";
                    } else if (user.getLastName().length() < 3) {
                        message = "Last Name should be minimum of 3 length";
                    } else {
                        message = "First Name should be minimum of 3 length";
                    }
                    response.setMessage(message);
                }
            } else {
                response.setSuccess(false);
                if (user.getEmailAddress().isEmpty()) {
                    message = "Email Address should not be empty!";
                } else if (user.getFirstName().isEmpty()) {
                    message = "First Name should not be empty!";
                } else if (user.getLastName().isEmpty()) {
                    message = "Last Name should not be empty!";
                } else if (user.getPassword().isEmpty()) {
                    message = "Password should not be empty!";
                }
                response.setMessage(message);
                log.info("Unable to create user!");
            }

        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
        return response;
    }
    public String generateUniquePostLink(String emailAddress) {
        try {
            // Combine emailAddress and timestamp
            String dataToHash = emailAddress + Instant.now().toString();

            // Create SHA-256 hash of the combined data
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes());

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hashStringBuilder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                // Convert each byte to a two-character hexadecimal representation
                String hex = String.format("%02x", hashByte);
                hashStringBuilder.append(hex);
            }

            return hashStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean ExistsEmail(String emailAddress) {
        User user= repository.findByEmailAddress(emailAddress);
        if(user != null){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public ResponseModel updateUserDetails(String id, User user, String authToken) {
        try {
            log.info("Validating Token!");
            if(jwtUtil.validateToken(authToken, id)) {
                if (user.getPassword() == null &&
                        user.getUserName() == null &&
                        user.getFirstName() == null &&
                        user.getEmailAddress() == null &&
                        user.getLastName() == null) {
                    response.setSuccess(false);
                    user.setStatus("online");
                    response.setMessage("Nothing to update!");
                } else {
                    Optional<User> oldUserDetails = repository.findById(id);
                    if (oldUserDetails.isPresent()) {
                        log.info("Update User - User found!");
                        User oldUser = oldUserDetails.get();
                        response = updateDetails(oldUser, user);
                        log.info("Response Fetched!");
                        if (response.isSuccess()) {
                            log.info("Response is success - Update User");
                            oldUser.setLastUpdateDate(LocalDateTime.now());
                            log.info("Updating last update date!");
                            repository.save(oldUser);
                            log.info("User updated successfully");
                        }
                    } else {
                        response.setSuccess(false);
                        response.setMessage("User doesn't Exists");
                        log.info("Unable to Update the user!");
                    }
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access");
            }
        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
        return response;
    }

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public ResponseModel deleteUser(String id, String authToken) {
        try {
            if(jwtUtil.validateToken(authToken, id)) {
                Optional<User> user = repository.findById(id);
                if (user.isPresent()) {
                    user.get().setStatus("Offline");
                    repository.deleteById(id);
                    connectionRepo.deleteAllByReceiverId(id);
                    connectionRepo.deleteAllBySenderId(id);
                    callRepository.deleteAllByCallerId(id);
                    callRepository.deleteAllByReceiverId(id);
                    postsRepository.deleteAllByUserId(id);
                    log.info("User Deleted Successfully!");
                    response.setSuccess(true);
                    response.setMessage("User Deleted Successfully!");
                } else {
                    log.info("Unable to delete user!");
                    response.setSuccess(false);
                    response.setMessage("User Doesn't Exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access");
                log.info("Unauthorized Access!");
            }
        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel userLogin(User user) {
        try{
            String emailAddress = user.getEmailAddress();
            String password = user.getPassword();
            log.info("Extracted details from json!");
            if(emailAddress.isEmpty()){
                response.setSuccess(false);
                response.setMessage("Enter email address!");
            }
            else if(password.isEmpty()){
                response.setSuccess(false);
                response.setMessage("Enter password!");
            }
            else if(!isValidEmail(emailAddress)){
                response.setSuccess(false);
                response.setMessage("Enter a valid email!");
            }
            else if(!isValidPassword(password)){
                response.setSuccess(false);
                response.setMessage("Enter a valid password!");
            }
            else {
                User fetchUser = repository.findByEmailAddress(emailAddress);
                log.info("Extracted user details from email address!");
                if (fetchUser != null) {
                    if (passwordEncoder.matches(password + iChatApplicationConstants.HASH_SALT, fetchUser.getPassword())) {
                        response.setSuccess(true);
                        fetchUser.setStatus("online");
                        String authToken = generateToken(fetchUser);
                        response.setMessage(authToken);
                        repository.save(fetchUser);
                        log.info("Logged in successfully!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Incorrect Credentials!");
                        log.info("Unable to login due to incorrect credentials!");
                    }
                } else {
                    response.setSuccess(false);
                    response.setMessage("User is not registered!");
                    log.info("User is not registered!");
                }
            }
        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
//        response.setSuccess(true);
//        response.setMessage("LOGGED IN!");
        return response;
    }

    @Override
    public User findDetailsUsingId(String id) {
        try {
            Optional<User> user = repository.findById(id);
            if(user.isPresent()){
                log.info("User Fetched!");
                User user1 = user.get();
                return user1;
            }
            else{
                log.info("User Doesn't Exists for id - "+id);
                return new User();
            }
        } catch (Exception e) {
            log.info("Error Occured : "+e.getMessage());
            return new User();
        }
    }

    @Override
    public ResponseModel resetPassword(User user) {
        String emailAddress = user.getEmailAddress();
        String password = user.getPassword();
        if(emailAddress.isEmpty()){
            response.setSuccess(false);
            log.info("Email Address is empty!");
            response.setMessage("Email Address should not be empty!");
        }
        else if (password.isEmpty()) {
            response.setSuccess(false);
            log.info("Password is empty!");
            response.setMessage("Password field should not be empty!");
        }
        else if(!isValidEmail(emailAddress)){
            response.setSuccess(false);
            log.info("Email is not in valid format!");
            response.setMessage("Enter a valid email address!");
        }
        else if(!isValidPassword(password)){
            response.setSuccess(false);
            log.info("Password is not in valid format!");
            response.setMessage("Enter a strong password with at least " +
                    "one upper case," +
                    "one lower case, and " +
                    "one special character - (#,@,$)");
        }
        else{
            User oldUser = repository.findByEmailAddress(emailAddress);
            if(oldUser==null){
                response.setSuccess(false);
                response.setMessage("Email is not registered!");
                log.info("Invalid user");
            }
            else {
                String hashedPassword = passwordEncoder.encode(user.getPassword() + iChatApplicationConstants.HASH_SALT);
                oldUser.setPassword(hashedPassword);
                repository.save(oldUser);
                response.setSuccess(true);
                response.setMessage("Password Reset Successfully!");
                log.info("Password reset successfully!");
            }
        }
        return response;
    }

    @Override
    public User findDetails(String authToken) {
        String id = jwtUtil.fetchId(authToken);
        return repository.findById(id).get();
    }

    @Override
    public ResponseModel loggedOut(String id, String authToken) {
        if(jwtUtil.validateToken(authToken, id)){
            User u = repository.findById(id).get();
            u.setStatus("offline");
            log.info("User with id - "+id+" is set to offline!");
            repository.save(u);
            response.setSuccess(true);
            response.setMessage("Logged Out Successfully!");
        }
        else{
            response.setSuccess(false);
            response.setMessage("Unauthorized Access!");
        }
        return response;
    }
    @Override
    public User fetchUserByLink(String profileLink) {
        try{
            User user = repository.findByProfileLink(profileLink);
            if(user!=null){
                log.info("User fetched successfully!");
                return user;
            }
            else{
                log.info("User Doesn't exists!");
            }
        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
        return null;
    }

    @Override
    public ResponseModel updateProfileLink(String id, String authToken, String profileLink) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = repository.findById(id);
                if(user.isPresent()){
                    User oldUser = user.get();
                    User prevUser = repository.findByProfileLink(profileLink);
                    if(prevUser!=null){
                        response.setSuccess(false);
                        response.setMessage("Profile Link is not unique!");
                        log.info("Profile link - "+profileLink+" is not unique!");
                    }
                    else{
                        oldUser.setProfileLink(profileLink);
                        repository.save(oldUser);
                        response.setSuccess(true);
                        response.setMessage("Profile Link updated successfully!");
                        log.info("Profile Link updated successfully!");
                    }
                }
                else{
                    response.setMessage("User doesn't exists!");
                    response.setSuccess(false);
                    log.info("User with id - "+id+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("Unauthorized Access for id - "+id);
            }
        }
        catch(Exception ex){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info(ex.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel setProfilePhoto(String id, String authToken, User user) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> u = repository.findById(id);
                if(u.isPresent()){
                    User u1 = u.get();
                    u1.setProfilePhoto(user.getProfilePhoto());
                    repository.save(u1);
                    log.info("Profile Photo updated successfully!");
                    response.setMessage("Profile Photo uploaded!");
                    response.setSuccess(true);
                }
                else{
                    log.info("User with id - "+id+" doesn't exists");
                    response.setSuccess(false);
                    response.setMessage("User doesn't exists!");
                }
            }
            else{
                log.info("Unauthorized Access with id - "+id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error: "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occured");
        }

        return response;
    }

    @Override
    public byte[] getProfilePhoto(String id) {
        try{
            Optional<User> isUser = repository.findById(id);
            if(isUser.isPresent()){
                User user = isUser.get();
                log.info("Fetched profile photo!");
                return user.getProfilePhoto();
            }
            else{
                log.info("Unauthorized Access with id - "+id);
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
            }
        }
        catch(Exception e){
            log.info("Error: "+e.getMessage());
            response.setSuccess(false);
            response.setMessage("Internal Error Occured");
        }
        return null;
    }

    @Override
    public List<Chat> fetchChats(String id, String authToken) {
        if(jwtUtil.validateToken(authToken, id)){
            Optional<User> userDetail = repository.findById(id);
            if(userDetail.isPresent()){
                User user = userDetail.get();
                HashMap<String, LocalDateTime> chatIds = user.getChatId();
//                System.out.println(chatIds);
                List<Chat> chats = fetchChatsFromId(chatIds, id);
                chats.sort(Comparator.comparing(Chat::getTimeStamp).reversed());
                log.info("Chats Fetched Successfully!");
                return chats;
            }
            else{
                log.info("User with id - "+id+" Doesn't Exists!");
            }
        }
        else{
            log.info("Unauthorized Access with id - "+id);
        }
        return Collections.emptyList();
    }

    private List<Chat> fetchChatsFromId(HashMap<String,LocalDateTime> chatIds, String userId) {
        if(chatIds!=null && !chatIds.isEmpty()){
            List<Chat> chats = new ArrayList<>();
            Iterator<String> chat = chatIds.keySet().iterator();
            while(chat.hasNext()){
                String id = chat.next();
                Chat c = chatRepository.findTopByChatIdOrderByTimeStampDesc(id);
                if(c!=null) {
                    if(c.getDeletedMessageUserId() == null || !c.getDeletedMessageUserId().contains(userId)) {
                        chats.add(c);
                    }
                    else{
                        List<Chat> chatHistory = chatRepository.findAllByChatIdOrderByTimeStampDesc(id);

                        // Filter out deleted chats
                        List<Chat> undeletedChats = findUndeletedChats(chatHistory, userId);

                        if (!undeletedChats.isEmpty()) {
                            // Sort undeleted chats by timestamp in descending order
                            undeletedChats.sort(Comparator.comparing(Chat::getTimeStamp).reversed());

                            // Add the latest undeleted chat to the result list
                            chats.add(undeletedChats.get(0));
                        } else {
                            log.info("No undeleted chat found for chatId " + id + "!");
                        }
                    }
                }
                else{
                    log.info("Chat doesn't exists - with id " +id+"!");
                    chat.remove();
                }
            }
            System.out.println(chats);
            return chats;
        }
        return Collections.emptyList();
    }

    private List<Chat> findUndeletedChats(List<Chat> chatHistory, String userId) {
        List<Chat> undeletedChats = new ArrayList<>();
        Iterator<Chat> chatIterator = chatHistory.iterator();

        while(chatIterator.hasNext()){
            Chat chat = chatIterator.next();
            if(chat.getDeletedMessageUserId()==null || !chat.getDeletedMessageUserId().contains(userId)){
                undeletedChats.add(chat);
            }
            else{
                chatIterator.remove();
            }
        }
        return undeletedChats;
    }

    public String generateToken(User userDetails) {
        log.info("Calling Method");
        return jwtUtil.generateToken(userDetails);
    }

    private ResponseModel updateDetails(User oldUser, User user) {
        if(user.getUserName()!=null){
            oldUser.setUserName(user.getUserName());
        }
        if(user.getPassword()!=null && !user.getPassword().isEmpty() && isValidPassword(user.getPassword())){
            String hashedPassword = passwordEncoder.encode(user.getPassword()+ iChatApplicationConstants.HASH_SALT);
            oldUser.setPassword(hashedPassword);
        }
        else if(user.getPassword()!=null && !isValidPassword(user.getPassword())){
            response.setSuccess(false);
            response.setMessage("Enter a strong password with at least " +
                    "one upper case," +
                    "one lower case, and " +
                    "one special character - (#,@,$)");
            return response;
        }
        else if(user.getPassword()!=null && user.getPassword().isEmpty()){
            response.setSuccess(false);
            response.setMessage("Password should not be empty!");
            return response;
        }
        if(user.getFirstName()!=null && user.getFirstName().length()>=3){
            oldUser.setFirstName(user.getFirstName());
        }
        else if(user.getFirstName()!=null && user.getFirstName().length()<3){
            response.setSuccess(false);
            response.setMessage("First Name should be minimum of 3 length");
            return response;
        }
        if(user.getLastName()!=null && user.getLastName().length()>=3){
            oldUser.setLastName(user.getLastName());
        }
        else if(user.getLastName()!=null && user.getLastName().length()<3){
            response.setSuccess(false);
            response.setMessage("Last Name should be minimum of 3 length");
            return response;
        }
        if(user.getTagLine()!=null){
            oldUser.setTagLine(user.getTagLine());
        }
        response.setSuccess(true);
        response.setMessage("User updated successfully");
        return response;
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$]).*$";

        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return password.length()>=5 && matcher.matches();
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
