package com.example.feedback.repository;

import com.example.feedback.entity.FeedbackUpload;
import com.example.feedback.entity.SentimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentimentResultRepository extends JpaRepository<SentimentResult, Long> {
    @Query("SELECT sr FROM SentimentResult sr WHERE sr.feedback.upload = :upload")
    List<SentimentResult> findByUpload(@Param("upload") FeedbackUpload upload);
    
    @Query("SELECT COUNT(sr) FROM SentimentResult sr WHERE sr.feedback.upload = :upload AND sr.sentiment = 'POSITIVE'")
    int countPositiveByUpload(@Param("upload") FeedbackUpload upload);

    @Query("SELECT COUNT(sr) FROM SentimentResult sr WHERE sr.feedback.upload = :upload AND sr.sentiment = 'NEGATIVE'")
    int countNegativeByUpload(@Param("upload") FeedbackUpload upload);

    @Query("SELECT COUNT(sr) FROM SentimentResult sr WHERE sr.feedback.upload = :upload AND sr.sentiment = 'NEUTRAL'")
    int countNeutralByUpload(@Param("upload") FeedbackUpload upload);
}
