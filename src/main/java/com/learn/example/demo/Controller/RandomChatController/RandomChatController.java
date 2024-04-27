package com.learn.example.demo.Controller.RandomChatController;

import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Service.RandomChatServices.RandomChatServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RandomChatController {

    @Autowired
    private RandomChatServiceImplementation service;

    @GetMapping("/get/random/{id}")
    public User fetchRandomUser(@PathVariable String id){
        return service.fetchRandomOnlineUser(id);
    }
}
