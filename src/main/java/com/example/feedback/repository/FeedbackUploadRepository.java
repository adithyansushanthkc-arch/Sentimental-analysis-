package com.example.feedback.repository;

import com.example.feedback.entity.FeedbackUpload;
import com.example.feedback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackUploadRepository extends JpaRepository<FeedbackUpload, Long> {
    List<FeedbackUpload> findByUploadedByOrderByUploadDateDesc(User uploadedBy);
    List<FeedbackUpload> findAllByOrderByUploadDateDesc();
}
