package com.learn.example.demo.Repository.ChatRepository;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatFeatureRepository extends MongoRepository<Chat, String> {
    List<Chat> findAllByChatId(String chatId);

    Chat findByChatIdAndMessageId(String chatId, String messageId);

    void deleteByChatIdAndMessageId(String chatId, String messageId);

    Chat findByChatIdOrderByTimeStampDesc(String id);

    List<Chat> findAllByChatIdOrderByTimeStampDesc(String chatId);

    List<Chat> findAllByChatIdOrderByTimeStamp(String chatId);

    Chat findTopByChatIdOrderByTimeStampDesc(String id);

    @Query("{ 'deletedUserId' : { $exists : true, $size: 2 } }")
    List<Chat> findByDeletedUserIdLengthTwo();

    void deleteAllByChatId(String chatId);
}
