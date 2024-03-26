package com.learn.example.demo.Service.LoginService;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;

import java.util.List;

public interface LoginServiceInterface {
   ResponseModel createUser(User user);

   ResponseModel updateUserDetails(String id, User user, String authToken);

   List<User> getAllUsers();

   ResponseModel deleteUser(String id, String authToken);

   ResponseModel userLogin(User user);

    User findDetailsUsingId(String id);

    ResponseModel resetPassword(User user);

    User findDetails(String authToken);

    ResponseModel loggedOut(String id, String authToken);
    User fetchUserByLink(String profileLink);

    ResponseModel updateProfileLink(String id, String authToken, String profileLink);

    ResponseModel setProfilePhoto(String id, String authToken, User user);

    byte[] getProfilePhoto(String id);

    List<Chat> fetchChats(String id, String authToken);
}
