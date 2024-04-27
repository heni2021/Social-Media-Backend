package com.learn.example.demo.Service.RandomChatServices;

import com.learn.example.demo.Models.LoginModels.User;

public interface RandomChatServiceInterface {
    User fetchRandomOnlineUser(String id);
}
