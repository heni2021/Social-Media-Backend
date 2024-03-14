package com.learn.example.demo.Controller.PostsController;

import com.learn.example.demo.Models.ResponsesModel.ImageRequestData;
import com.learn.example.demo.Models.PostsModel.Posts;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Service.PostsService.PostsServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
public class PostsController {

    // TODO: Remove Likes function

    @Autowired
    private PostsServiceImplementation service;

    @PostMapping("/addPost/{id}")
    public ResponseModel addNewPosts(@PathVariable String id, HttpServletRequest request, @RequestBody ImageRequestData requestData) {
        String image = requestData.getImage();
        byte[] img = Base64.getDecoder().decode(image);
        Posts post = new Posts();
        post.setDescription(requestData.getDescription());
        post.setImage(img);
        String authToken = request.getHeader("auth-token");
        return service.addNewPost(id, authToken, post);
    }

    @PostMapping("downloadPost/{id}/{postId}/{format}")
    public ResponseEntity<byte[]> downloadPost(@PathVariable String id, @PathVariable String format, @PathVariable String postId, HttpServletRequest request, HttpServletResponse response){
        String authToken = request.getHeader("auth-token");
        return service.downloadImage(authToken, id, postId, format, response);
    }

    @PutMapping("/updatePost/{id}/{postId}")
    public ResponseModel updatePost(@PathVariable String id, @PathVariable String postId, HttpServletRequest request, @RequestBody ImageRequestData requestData){
        String authToken = request.getHeader("auth-token");
        Posts post = new Posts();
        if(requestData.getImage()!=null) {
            String image = requestData.getImage();
            byte[] img = Base64.getDecoder().decode(image);
            post.setImage(img);
        }
        post.setDescription(requestData.getDescription());
        return service.updatePost(id, authToken, post, postId);
    }

    @DeleteMapping("/deletePost/{id}/{postId}")
    public ResponseModel deletePost(@PathVariable String id, HttpServletRequest request, @PathVariable String postId){
        String authToken = request.getHeader("auth-token");
        return service.deletePost(id, authToken, postId);
    }

    @GetMapping("/get/{postId}")
    public Posts getPost(@PathVariable String postId){
        return service.getPost(postId);
    }

    @PutMapping("/updateLikeCount/{userId}/{postId}")
    public ResponseModel updateLikeCount(@PathVariable String userId, @PathVariable String postId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return service.updateLikes(userId, postId, authToken);
    }

    @GetMapping("/isLiked/{userId}/{postId}")
    public ResponseModel checkLiked(@PathVariable String userId, @PathVariable String postId){
        return service.checkIsLiked(userId, postId);
    }

    @PutMapping("/removeLike/{userId}/{postId}")
    public ResponseModel removeLikeCount(@PathVariable String userId, @PathVariable String postId, HttpServletRequest request){
        String authToken = request.getHeader("auth-token");
        return service.removeLike(userId, postId, authToken);
    }

    @GetMapping("/post/byFollowing/{id}")
    public List<Posts> fetchPostByFollowing(@PathVariable String id){
        return service.fetchByFollowing(id);
    }

    @GetMapping("/post/fetchByLink")
    public Posts fetchPostByLink(@RequestBody Posts post){
        return service.fetchPostByLink(post.getPostLink());
    }

    @GetMapping("/post/getAll/{id}")
    public List<Posts> getAllPostById(@PathVariable String id){
        return service.getAllPostById(id);
    }
}
