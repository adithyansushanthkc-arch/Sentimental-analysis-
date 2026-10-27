package com.example.feedback.repository;

import com.example.feedback.entity.ActivityType;
import com.example.feedback.entity.User;
import com.example.feedback.entity.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findTop50ByOrderByTimestampDesc();

    List<UserActivity> findByUserOrderByTimestampDesc(User user);

    long countByActivityType(ActivityType activityType);

    long countByUser(User user);

    long countByUserAndActivityType(User user, ActivityType activityType);

    @Query("SELECT a FROM UserActivity a ORDER BY a.timestamp DESC")
    List<UserActivity> findRecentActivities(Pageable pageable);
}
