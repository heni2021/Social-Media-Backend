package com.learn.example.demo.Repository.PostsRepository;

import com.learn.example.demo.Models.PostsModel.Posts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends MongoRepository<Posts, String> {
   Posts findByPostLink(String postLink);

   List<Posts> findByUserId(String id);

   void deleteAllByUserId(String id);
}
