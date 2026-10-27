package com.example.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_result")
public class SentimentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "feedback_id", nullable = false, unique = true)
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SentimentType sentiment;

    @Column(name = "sentiment_score", nullable = false)
    private Integer sentimentScore;

    @Column(name = "positive_word_count", nullable = false)
    private Integer positiveWordCount;

    @Column(name = "negative_word_count", nullable = false)
    private Integer negativeWordCount;

    @CreationTimestamp
    @Column(name = "analyzed_at", updatable = false)
    private LocalDateTime analyzedAt;

    public SentimentResult() {}

    public SentimentResult(Long id, Feedback feedback, SentimentType sentiment, Integer sentimentScore,
                           Integer positiveWordCount, Integer negativeWordCount, LocalDateTime analyzedAt) {
        this.id = id;
        this.feedback = feedback;
        this.sentiment = sentiment;
        this.sentimentScore = sentimentScore;
        this.positiveWordCount = positiveWordCount;
        this.negativeWordCount = negativeWordCount;
        this.analyzedAt = analyzedAt;
    }

    public static SentimentResultBuilder builder() {
        return new SentimentResultBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }

    public SentimentType getSentiment() { return sentiment; }
    public void setSentiment(SentimentType sentiment) { this.sentiment = sentiment; }

    public Integer getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Integer sentimentScore) { this.sentimentScore = sentimentScore; }

    public Integer getPositiveWordCount() { return positiveWordCount; }
    public void setPositiveWordCount(Integer positiveWordCount) { this.positiveWordCount = positiveWordCount; }

    public Integer getNegativeWordCount() { return negativeWordCount; }
    public void setNegativeWordCount(Integer negativeWordCount) { this.negativeWordCount = negativeWordCount; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

    public static class SentimentResultBuilder {
        private Long id;
        private Feedback feedback;
        private SentimentType sentiment;
        private Integer sentimentScore;
        private Integer positiveWordCount;
        private Integer negativeWordCount;
        private LocalDateTime analyzedAt;

        public SentimentResultBuilder id(Long id) { this.id = id; return this; }
        public SentimentResultBuilder feedback(Feedback feedback) { this.feedback = feedback; return this; }
        public SentimentResultBuilder sentiment(SentimentType sentiment) { this.sentiment = sentiment; return this; }
        public SentimentResultBuilder sentimentScore(Integer sentimentScore) { this.sentimentScore = sentimentScore; return this; }
        public SentimentResultBuilder positiveWordCount(Integer positiveWordCount) { this.positiveWordCount = positiveWordCount; return this; }
        public SentimentResultBuilder negativeWordCount(Integer negativeWordCount) { this.negativeWordCount = negativeWordCount; return this; }
        public SentimentResultBuilder analyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; return this; }

        public SentimentResult build() {
            return new SentimentResult(id, feedback, sentiment, sentimentScore, positiveWordCount, negativeWordCount, analyzedAt);
        }
    }
}
