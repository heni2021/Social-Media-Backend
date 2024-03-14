package com.learn.example.demo.Service.PostsService;

import com.learn.example.demo.Models.PostsModel.Posts;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PostsServiceInterface {
    ResponseModel addNewPost(String id, String authToken, Posts post);

    ResponseModel updatePost(String id, String authToken, Posts post, String postId);

    ResponseModel deletePost(String id, String authToken, String postId);

    Posts getPost(String postId);

    ResponseModel updateLikes(String userId, String postId, String authToken);

    ResponseModel removeLike(String userId, String postId, String authToken);

    Posts fetchPostByLink(String postLink);

    List<Posts> getAllPostById(String id);

    List<Posts> fetchByFollowing(String id);

    ResponseModel checkIsLiked(String userId, String postId);

    ResponseEntity<byte[]> downloadImage(String authToken, String id, String postId, String format, jakarta.servlet.http.HttpServletResponse responseClient);
}
