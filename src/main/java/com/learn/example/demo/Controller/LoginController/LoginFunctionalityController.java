package com.learn.example.demo.Controller.LoginController;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Models.ResponsesModel.ImageRequestData;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Service.LoginService.LoginServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/user")
public class LoginFunctionalityController {
    //TODO: Implement signup with google functionality
    // TODO: Gender wise icon in profile 
    @Autowired
    private LoginServiceImplementation serviceImplementation;

    @PostMapping("/add")
    public ResponseModel addNewUser(@RequestBody User user){
        user.setCreationDate(LocalDateTime.now());
        user.setLastUpdateDate(LocalDateTime.now());
        return serviceImplementation.createUser(user);
    }

    @GetMapping("/findDetails")
    public User findDetails(HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.findDetails(authToken);
    }

    @PostMapping("/login")
    public ResponseModel userLogin(@RequestBody User user){
        return serviceImplementation.userLogin(user);
    }

    @PutMapping("/update/{id}")
    public ResponseModel updateUser(@PathVariable String id, @RequestBody User user, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.updateUserDetails(id, user, authToken);
    }

    @GetMapping("/getUsers")
    public List<User> getUserDetails(){
        return serviceImplementation.getAllUsers();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseModel deleteUser(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.deleteUser(id, authToken);
    }

    @GetMapping("/fetchDetails/{id}")
    public User fetchDetails(@PathVariable String id){
        return serviceImplementation.findDetailsUsingId(id);
    }

    @PostMapping("/forgetPassword")
    public ResponseModel resetPassword(@RequestBody User user){
        return serviceImplementation.resetPassword(user);
    }

    @PostMapping("/logout/{id}")
    public ResponseModel loggedOut(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.loggedOut(id, authToken);
    }

    @GetMapping("/fetchByLink")
    public User fetchUserByLink(@RequestBody User user){
        return serviceImplementation.fetchUserByLink(user.getProfileLink());
    }
    @PutMapping("/updateProfileLink/{id}")
    public ResponseModel updateProfileLink(@PathVariable String id, HttpServletRequest request, @RequestBody User user){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.updateProfileLink(id, authToken, user.getProfileLink());
    }

    @PutMapping("/setProfilePhoto/{id}")
    public ResponseModel setProfilePhoto(@PathVariable String id, HttpServletRequest request,@RequestBody ImageRequestData requestData){
        String authToken = request.getHeader("auth-token");
        User user = new User();
        if(requestData.getImage()!=null) {
            String image = requestData.getImage();
            byte[] img = Base64.getDecoder().decode(image);
            user.setProfilePhoto(img);
        }
        return serviceImplementation.setProfilePhoto(id, authToken, user);
    }

    @GetMapping("/fetchProfilePhoto/{id}")
    public byte[] getProfilePhoto(@PathVariable String id){
        return serviceImplementation.getProfilePhoto(id);
    }

    @GetMapping("/fetchChatDetails/{id}")
    public List<Chat> fetchChatDetails(@PathVariable String id, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return serviceImplementation.fetchChats(id, authToken);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex){
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
