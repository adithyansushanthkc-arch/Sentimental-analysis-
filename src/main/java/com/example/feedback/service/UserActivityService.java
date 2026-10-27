package com.example.feedback.service;

import com.example.feedback.entity.ActivityType;
import com.example.feedback.entity.User;
import com.example.feedback.entity.UserActivity;
import com.example.feedback.repository.UserActivityRepository;
import com.example.feedback.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActivityService {

    private final UserActivityRepository activityRepository;
    private final UserRepository userRepository;

    public UserActivityService(UserActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logActivity(User user, ActivityType activityType, String details) {
        UserActivity activity = new UserActivity(user, activityType, details);
        activityRepository.save(activity);

        if (user != null && activityType == ActivityType.LOGIN) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Transactional
    public void logActivityByEmail(String email, ActivityType activityType, String details) {
        if (email == null || email.isBlank()) return;
        User user = userRepository.findByEmail(email).orElse(null);
        logActivity(user, activityType, details);
    }

    public List<UserActivity> getRecentActivities(int limit) {
        return activityRepository.findRecentActivities(PageRequest.of(0, limit));
    }

    public List<UserActivity> getAllActivities() {
        return activityRepository.findTop50ByOrderByTimestampDesc();
    }

    public long getTotalLogins() {
        return activityRepository.countByActivityType(ActivityType.LOGIN);
    }

    public long getUserActivityCount(User user) {
        if (user == null) return 0;
        return activityRepository.countByUser(user);
    }

    public long getUserAnalysisCount(User user) {
        if (user == null) return 0;
        return activityRepository.countByUserAndActivityType(user, ActivityType.SENTIMENT_ANALYSIS);
    }

    public long getUserUploadCount(User user) {
        if (user == null) return 0;
        return activityRepository.countByUserAndActivityType(user, ActivityType.FEEDBACK_UPLOAD);
    }
}
