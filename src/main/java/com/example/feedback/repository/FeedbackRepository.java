package com.example.feedback.repository;

import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.FeedbackUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUpload(FeedbackUpload upload);
    int countByUpload(FeedbackUpload upload);
}
