package com.learn.example.demo.Repository.LoginRepository;

import com.learn.example.demo.Models.ConnectionModels.FriendRequest;
import com.learn.example.demo.Models.LoginModels.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginFunctionalityRepository extends MongoRepository<User, String> {

    User findByEmailAddress(String emailAddress);

    @Query("{'id': {$ne: ?0}, 'id': {$nin: ?1}, 'id': {$nin: ?2}}")
    List<User> findUsersNotInFollowingAndFriendRequests(String id, List<User> followingIds, List<FriendRequest> incomingFriendRequest, List<FriendRequest> outgoingFriend);

    User findByProfileLink(String profileLink);

    List<User> findByUserNameLikeOrFirstNameLikeOrLastNameLike(String regexPattern, String firstNamePattern, String LastNamePattern);
}
