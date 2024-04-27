package com.learn.example.demo.Service.RandomChatServices;

import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class RandomChatServiceImplementation implements  RandomChatServiceInterface{
    @Autowired
    private LoginFunctionalityRepository loginRepository;

    @Override
    public User fetchRandomOnlineUser(String id) {
        try{
            List<User> currentOnlineUsers = fetchOnlineUserExceptId(id);
            log.info("Fetched All Online Users successfully!!");
            long index = generateRandomIndex(0, currentOnlineUsers.size());
            log.info("Index Generated - "+index);
            return currentOnlineUsers.get((int) index);
        }
        catch(Exception e){
            log.info("Error Occured: "+e.getMessage());
        }
        return null;
    }

    private long generateRandomIndex(int i, int size) {
        Random random = new Random();
        return random.nextInt(size - i) + i;
    }

    private List<User> fetchOnlineUserExceptId(String id) {
        List<User> users = loginRepository.findOnlineUsersExceptId(id);
        return users;
    }
}
