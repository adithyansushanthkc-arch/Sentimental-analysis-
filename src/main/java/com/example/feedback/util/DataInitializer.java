package com.example.feedback.util;

import com.example.feedback.entity.Role;
import com.example.feedback.entity.SentimentDictionary;
import com.example.feedback.entity.SentimentType;
import com.example.feedback.entity.User;
import com.example.feedback.service.DictionaryService;
import com.example.feedback.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final DictionaryService dictionaryService;

    public DataInitializer(UserService userService, DictionaryService dictionaryService) {
        this.userService = userService;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize Default Admin
        if (userService.getUserByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@example.com")
                    .password("admin123") // Hashed in UserService
                    .role(Role.ADMIN)
                    .status("Active")
                    .build();
            userService.saveUser(admin);
        }

        // Initialize Default Client (client@example.com)
        if (userService.getUserByEmail("client@example.com").isEmpty()) {
            User client = User.builder()
                    .name("Faculty Member")
                    .email("client@example.com")
                    .password("client123") // Hashed in UserService
                    .role(Role.CLIENT)
                    .status("Active")
                    .build();
            userService.saveUser(client);
        }

        // Initialize Faculty Client (faculty@example.com)
        if (userService.getUserByEmail("faculty@example.com").isEmpty()) {
            User faculty = User.builder()
                    .name("Dr. Smith")
                    .email("faculty@example.com")
                    .password("faculty123") // Hashed in UserService
                    .role(Role.CLIENT)
                    .status("Active")
                    .build();
            userService.saveUser(faculty);
        }

        // Initialize Default Dictionary if empty
        if (dictionaryService.getAllWords().isEmpty()) {
            List<SentimentDictionary> initialWords = Arrays.asList(
                    new SentimentDictionary(null, "excellent", SentimentType.POSITIVE, 2, "Active", null),
                    new SentimentDictionary(null, "good", SentimentType.POSITIVE, 1, "Active", null),
                    new SentimentDictionary(null, "great", SentimentType.POSITIVE, 2, "Active", null),
                    new SentimentDictionary(null, "amazing", SentimentType.POSITIVE, 2, "Active", null),
                    new SentimentDictionary(null, "helpful", SentimentType.POSITIVE, 1, "Active", null),
                    new SentimentDictionary(null, "clear", SentimentType.POSITIVE, 1, "Active", null),
                    new SentimentDictionary(null, "effective", SentimentType.POSITIVE, 1, "Active", null),
                    new SentimentDictionary(null, "friendly", SentimentType.POSITIVE, 1, "Active", null),
                    new SentimentDictionary(null, "best", SentimentType.POSITIVE, 2, "Active", null),
                    
                    new SentimentDictionary(null, "bad", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "poor", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "worst", SentimentType.NEGATIVE, -2, "Active", null),
                    new SentimentDictionary(null, "difficult", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "unclear", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "boring", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "useless", SentimentType.NEGATIVE, -2, "Active", null),
                    new SentimentDictionary(null, "unsatisfied", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "slow", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "confusing", SentimentType.NEGATIVE, -1, "Active", null),
                    new SentimentDictionary(null, "terrible", SentimentType.NEGATIVE, -2, "Active", null)
            );
            
            for (SentimentDictionary word : initialWords) {
                dictionaryService.saveWord(word);
            }
        }
    }
}
