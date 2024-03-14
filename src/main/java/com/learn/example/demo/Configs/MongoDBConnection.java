package com.learn.example.demo.Configs;

import com.learn.example.demo.iChatApplication;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.learn.example")
public class MongoDBConnection extends AbstractMongoClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(iChatApplication.class);

    @Value("${spring.data.mongodb.uri}")
    private String url;

    @Override
    protected String getDatabaseName() {
        return "iChat";
    }

    @Override
    public MongoClient mongoClient() {
        log.info("MongoDB Database Connected Successfully!");
        return MongoClients.create(url);
    }

}
