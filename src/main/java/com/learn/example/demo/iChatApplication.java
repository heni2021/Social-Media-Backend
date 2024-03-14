package com.learn.example.demo;

import com.learn.example.demo.Models.ChatFeatureModels.Chat;
import com.learn.example.demo.Repository.ChatRepository.ChatFeatureRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Iterator;
import java.util.List;

@SpringBootApplication
@EnableWebSecurity
@Slf4j
public class iChatApplication {
	@Autowired
	private ChatFeatureRepository chatFeatureRepository;

	public static void main(String[] args) {
		SpringApplication.run(iChatApplication.class, args);
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.anyRequest().permitAll()
				.and()
				.csrf(csrf -> csrf.disable()); // Permit CSRF for specific endpoints

		return http.build();
	}

	@PostConstruct
	public void clearUnwantedChats(){
		List<Chat> unwantedChats = chatFeatureRepository.findByDeletedUserIdLengthTwo();
		Iterator<Chat> chatIterator = unwantedChats.iterator();
		while(chatIterator.hasNext()){
			Chat chat = chatIterator.next();
			chatFeatureRepository.deleteByChatIdAndMessageId(chat.getChatId(), chat.getMessageId());
		}
		log.info("Unwanted Chats Cleared from database!!");
	}
}
