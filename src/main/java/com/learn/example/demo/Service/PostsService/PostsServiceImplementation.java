package com.learn.example.demo.Service.PostsService;

import com.learn.example.demo.iChatApplication;
import com.learn.example.demo.Models.PostsModel.Posts;
import com.learn.example.demo.Models.ResponsesModel.ResponseModel;
import com.learn.example.demo.Models.LoginModels.User;
import com.learn.example.demo.Repository.LoginRepository.LoginFunctionalityRepository;
import com.learn.example.demo.Repository.PostsRepository.PostsRepository;
import com.learn.example.demo.Utility.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostsServiceImplementation implements PostsServiceInterface{
    private static final Logger log = LoggerFactory.getLogger(iChatApplication.class);
    @Autowired
    private PostsRepository repository;

    @Autowired
    private LoginFunctionalityRepository loginRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ResponseModel response;

    public PostsServiceImplementation() {
        this.response = new ResponseModel();
    }

    @Override
    public ResponseModel addNewPost(String id, String authToken, Posts post) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = loginRepository.findById(id);
                if(user.isPresent()){
                    post.setPostingDate(LocalDateTime.now());
                    post.setLikesCount(0L);
                    post.setDownloadsCount(0L);
                    post.setUserId(id);
                    post.setLastUpdateDate(LocalDateTime.now());
                    post.setListOfUserLikes(new ArrayList<>());
                    post.setListOfDownloadUsers(new ArrayList<>());
                    String link = "https://posts:open/"+user.get().getUserName()+"/"+generateUniquePostLink(post.getDescription());
                    post.setPostLink(link);
                    repository.save(post);
                    log.info("Post Saved Successfully!");

                    User u = user.get();
                    if(u.getPostsId()==null){
                        u.setPostsId(new ArrayList<>());
                    }
                    List<String> postId = u.getPostsId();
                    postId.add(post.getId());
                    u.setPostsId(postId);
                    loginRepository.save(u);
                    log.info("Post got saved in user model successfully!");

                    response.setMessage("Posted Successfully!");
                    response.setSuccess(true);
                    log.info("Post with id - "+post.getId()+" posted successfully!");
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User Doesn't exists");
                    log.info("User with id - "+id+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("User with id - "+id+" is unauthorized!");
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    private String generateUniquePostLink(String postContent) {
        try {
            // Combine post content and timestamp
            String dataToHash = postContent + Instant.now().toString();

            // Create SHA-256 hash of the combined data
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes());

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hashStringBuilder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hashStringBuilder.append(String.format("%02x", hashByte));
            }

            return hashStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseModel updatePost(String id, String authToken, Posts post, String postId) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = loginRepository.findById(id);
                if(user.isPresent()){
                    Optional<Posts> oldPost = repository.findById(postId);
                    if(oldPost.isPresent()){
                        Posts updatedOldPost = updateOldPost(oldPost.get(), post);
                        updatedOldPost.setLastUpdateDate(LocalDateTime.now());
                        repository.save(updatedOldPost);
                        response.setSuccess(true);
                        response.setMessage("Post updated successfully!");
                        log.info("Post with id - "+postId+" updated successfully!");
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Posts Doesn't exists");
                        log.info("Post with id - "+postId+" doesn't exists!");
                    }
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User Doesn't exists");
                    log.info("User with id - "+id+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("User with id - "+id+" is unauthorized!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel deletePost(String id, String authToken, String postId) {
        try {
            if(jwtUtil.validateToken(authToken, id)){
                Optional<User> user = loginRepository.findById(id);
                if(user.isPresent()){
                    Optional<Posts> oldPost = repository.findById(postId);
                    if(oldPost.isPresent()){
                        repository.deleteById(postId);

                        User u = user.get();
                        List<String> userPost = u.getPostsId();
                        userPost.remove(postId);
                        u.setPostsId(userPost);
                        loginRepository.save(u);
                        log.info("Post removed from user!");

                        response.setSuccess(true);
                        response.setMessage("Post deleted successfully!");
                        log.info("Post with id - "+postId+" deleted successfully!");
                    }
                    else{
                        response.setSuccess(false);
                        response.setMessage("Posts Doesn't exists");
                        log.info("Post with id - "+postId+" doesn't exists!");
                    }
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("User Doesn't exists");
                    log.info("User with id - "+id+" doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("Unauthorized Access!");
                log.info("User with id - "+id+" is unauthorized!");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public Posts getPost(String postId) {
        try{
            Optional<Posts> posts = repository.findById(postId);
            if(posts.isPresent()){
                return posts.get();
            }
            else{
                response.setMessage("Post with id - "+postId+" Doesn't Exists");
                response.setSuccess(false);
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return null;
    }

    @Override
    public ResponseModel updateLikes(String userId, String postId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, userId)) {
//                System.out.println(postId);
                Optional<Posts> post = repository.findById(postId);
                if (post.isPresent()) {
                    Posts oldPost = post.get();
                    if(oldPost.getUserId().equals(userId)){
                        response.setSuccess(false);
                        response.setMessage("You cannot like your own post");
                        log.info("Can't like your own post!");
                    }
                    else {
                        if(oldPost.getListOfUserLikes()==null){
                            oldPost.setListOfUserLikes(new ArrayList<>());
                            List<String> userLikes = oldPost.getListOfUserLikes();
                            userLikes.add(userId);

                            oldPost.setLikesCount(oldPost.getLikesCount() + 1);
                            oldPost.setListOfUserLikes(userLikes);

                            repository.save(oldPost);
                            response.setSuccess(true);
                            response.setMessage("Post Liked successfully!");
                            log.info("Likes Increased Successfully for id - " + postId);
                        }
                        else{
                            List<String> likeUserPost = oldPost.getListOfUserLikes();
                            if(likeUserPost.contains(userId)){
                                response.setSuccess(false);
                                response.setMessage("You have already liked the post!");
                                log.info("Already liked the post");
                            }
                            else{
                                likeUserPost.add(userId);

                                oldPost.setLikesCount(oldPost.getLikesCount() + 1);
                                oldPost.setListOfUserLikes(likeUserPost);

                                repository.save(oldPost);
                                response.setSuccess(true);
                                response.setMessage("Post Liked successfully!");
                                log.info("Likes Increased Successfully for id - " + postId);
                            }
                        }
                    }
                }
                else {
                    response.setSuccess(false);
                    response.setMessage("Post doesn't exists!");
                    log.info("Post with id - " + postId + " doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("User not authorized!");
                log.info("User with id - " + userId + " doesn't exists!");
            }
        }
        catch (Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseModel removeLike(String userId, String postId, String authToken) {
        try{
            if(jwtUtil.validateToken(authToken, userId)){
                Optional<Posts> post = repository.findById(postId);
                if(post.isPresent()){
                    Posts oldPost = post.get();
                    if(oldPost.getUserId().equals(userId)){
                        response.setSuccess(false);
                        response.setMessage("You cannot react your own post");
                        log.info("Can't react your own post!");
                    }
                    else{
                        List<String> likedUser = oldPost.getListOfUserLikes();
                        if(likedUser.contains(userId)){
                            likedUser.remove(userId);
                            oldPost.setListOfUserLikes(likedUser);
                            oldPost.setLikesCount(oldPost.getLikesCount()-1);
                            repository.save(oldPost);
                            response.setSuccess(true);
                            response.setMessage("Post disliked successfully!");
                            log.info("Post with id - "+postId+" disliked successfully!");
                        }
                        else{
                            response.setSuccess(false);
                            response.setMessage("You have never liked this post!");
                            log.info("Can't dislike the post!");
                        }
                    }
                }
                else{
                    response.setSuccess(false);
                    response.setMessage("Post doesn't exists!");
                    log.info("Post with id - " + postId + " doesn't exists!");
                }
            }
            else{
                response.setSuccess(false);
                response.setMessage("User not authorized!");
                log.info("User with id - " + userId + " doesn't exists!");
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return response;
    }

    @Override
    public Posts fetchPostByLink(String postLink) {
        try{
            Posts post = repository.findByPostLink(postLink);
            if(post!=null) {
                log.info("Post Fetched by link successfully!");
                return post;
            }
        }
        catch(Exception e){
            response.setSuccess(false);
            response.setMessage("Some Error Occured!");
            log.info("Error: "+e.getMessage());
        }
        return null;
    }

    @Override
    public List<Posts> getAllPostById(String id) {
        try{
            List<Posts> posts = repository.findByUserId(id);
            List<Posts> sortedPosts = posts.stream()
                    .sorted((post1, post2) -> post2.getPostingDate().compareTo(post1.getPostingDate()))
                    .collect(Collectors.toList());

            log.info("All posts fetched and sorted successfully!");
            return sortedPosts;
        }
        catch(Exception e){
            log.info(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Posts> fetchByFollowing(String id) {
        try{
            Optional<User> u = loginRepository.findById(id);
            if(u.isPresent()){
                User user = u.get();
                List<String> following = user.getFollowingId();
                if(following!=null && following.size()>0){
                    List<String> postIds = new ArrayList<>();
                    List<Posts> posts = new ArrayList<>();
                    for (int i = 0; i < following.size(); i++) {
                        postIds = fetchPostById(following.get(i));
                        for (int j = 0; j < postIds.size(); j++) {
                            Posts p = getPost(postIds.get(j));
                            if(p!=null){
                                posts.add(p);
                            }
                        }
                    }

                    // Sorting by date
                    Collections.sort(posts, (post1, post2) -> post2.getPostingDate().compareTo(post1.getPostingDate()));
                    log.info("Posts of following fetched successfully!");
                    return posts;
                }
            }
            else{
                log.info("User with id - "+id+" doesn't exist!");
            }
        }
        catch(Exception e){
            log.info(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public ResponseModel checkIsLiked(String userId, String postId) {
        try{
           Optional<Posts> post = repository.findById(postId);
           if(post.isPresent()){
               List<String> likes = post.get().getListOfUserLikes();
               if(likes!=null && likes.contains(userId)){
                   response.setMessage("true");
                   response.setSuccess(true);
                   log.info("Post Already Liked by id -" +userId);
               }
               else{
                   response.setMessage("false");
                   response.setSuccess(true);
                   log.info("Post Not Liked by id -" +userId);
               }
           }
           else{
               response.setMessage("Post doesn't Exists!");
               response.setSuccess(false);
               log.info("Post Already Liked by id -" +userId);
           }
        }
        catch(Exception e){
            log.info(e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseEntity<byte[]> downloadImage(String authToken, String id, String postId, String format, HttpServletResponse responseClient) {
        try{
            if(jwtUtil.validateToken(authToken, id)){
                Optional<Posts> p = repository.findById(postId);
                if(p.isPresent()){
                    byte[] image = p.get().getImage();
                    Optional<User> u = loginRepository.findById(p.get().getUserId());
                    if(u.isPresent()){
                        String fileName = u.get().getFirstName().substring(0, 2) +"_"+u.get().getLastName().substring(u.get().getLastName().length() - 2, u.get().getLastName().length());
                        log.info("File Name: "+fileName);
                        if ("png".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.parseMediaType("image/" + format));
                            headers.setContentDispositionFormData("inline", fileName + "." + format);
                            log.info("Content-Disposition header: {}", headers.getContentDisposition());
                            log.info("Post downloaded Successfully");
                            updateDownloadCount(id, p);
                            return new ResponseEntity<>(image, headers, HttpStatus.OK);
                        }
                    }
                    else{
                        response.setMessage("User doesn't exists!");
                        response.setSuccess(false);
                        log.info("User with id - "+id+" doesn't exists!");
                    }
                }
                else{
                    log.info("Post with id - "+postId+" doesn't exists!");
                }
            }
            else{
                log.info("Unauthorized Access for id - "+id);

            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            log.info("Error: "+e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void updateDownloadCount(String id, Optional<Posts> p) {
        Posts post = p.get();
        if(post.getListOfDownloadUsers()==null){
            post.setListOfDownloadUsers(new ArrayList<>());
        }
        if(!post.getListOfDownloadUsers().contains(id)) {
            List<String> users = post.getListOfDownloadUsers();
            users.add(id);
            post.setListOfDownloadUsers(users);
            post.setDownloadsCount((long) post.getListOfDownloadUsers().size());
        }
        repository.save(post);
    }


    private List<String> fetchPostById(String id) {
        try{
            Optional<User> u = loginRepository.findById(id);
            if(u.isPresent()){
                User user = u.get();
                log.info("Post id fetched successfully!");
                return user.getPostsId();
            }
        }
        catch(Exception e){
            log.info(e.getMessage());
        }
        return Collections.emptyList();
    }

    private Posts updateOldPost(Posts oldPost, Posts newPost) {
        if(newPost.getDescription()!=null && !newPost.getDescription().equals(oldPost.getDescription())){
            oldPost.setDescription(newPost.getDescription());
        }
        if(newPost.getImage()!=null && !Arrays.equals(oldPost.getImage(), newPost.getImage())){
            oldPost.setImage(newPost.getImage());
        }
        return oldPost;
    }
}
