package com.example.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_dictionary")
public class SentimentDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_type", nullable = false)
    private SentimentType sentimentType;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    private String status; // Active, Inactive

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public SentimentDictionary() {}

    public SentimentDictionary(Long id, String word, SentimentType sentimentType, Integer weight, String status, LocalDateTime createdAt) {
        this.id = id;
        this.word = word;
        this.sentimentType = sentimentType;
        this.weight = weight;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SentimentDictionaryBuilder builder() {
        return new SentimentDictionaryBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public SentimentType getSentimentType() { return sentimentType; }
    public void setSentimentType(SentimentType sentimentType) { this.sentimentType = sentimentType; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class SentimentDictionaryBuilder {
        private Long id;
        private String word;
        private SentimentType sentimentType;
        private Integer weight;
        private String status;
        private LocalDateTime createdAt;

        public SentimentDictionaryBuilder id(Long id) { this.id = id; return this; }
        public SentimentDictionaryBuilder word(String word) { this.word = word; return this; }
        public SentimentDictionaryBuilder sentimentType(SentimentType sentimentType) { this.sentimentType = sentimentType; return this; }
        public SentimentDictionaryBuilder weight(Integer weight) { this.weight = weight; return this; }
        public SentimentDictionaryBuilder status(String status) { this.status = status; return this; }
        public SentimentDictionaryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SentimentDictionary build() {
            return new SentimentDictionary(id, word, sentimentType, weight, status, createdAt);
        }
    }
}
