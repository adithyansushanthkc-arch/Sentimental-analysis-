package com.example.feedback.repository;

import com.example.feedback.entity.SentimentDictionary;
import com.example.feedback.entity.SentimentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SentimentDictionaryRepository extends JpaRepository<SentimentDictionary, Long> {
    Optional<SentimentDictionary> findByWord(String word);
    List<SentimentDictionary> findByStatus(String status);
    List<SentimentDictionary> findBySentimentType(SentimentType type);
    int countBySentimentType(SentimentType type);
}
