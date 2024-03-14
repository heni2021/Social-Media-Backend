package com.learn.example.demo.Models.PostsModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Posts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Posts {
    @Id
    private String id;
    private String userId;
    private LocalDateTime postingDate;
    private LocalDateTime lastUpdateDate;
    private Long likesCount=0L;
    private Long downloadsCount = 0L;
    private String description;
    private byte[] image;
    private List<String> listOfUserLikes;
    private List<String> listOfDownloadUsers;
    private String postLink;
}
