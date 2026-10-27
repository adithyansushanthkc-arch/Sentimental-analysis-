package com.example.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_record")
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false, unique = true)
    private FeedbackUpload upload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_feedback")
    private Integer totalFeedback;

    @Column(name = "positive_count")
    private Integer positiveCount;

    @Column(name = "negative_count")
    private Integer negativeCount;

    @Column(name = "neutral_count")
    private Integer neutralCount;

    @Column(name = "average_score")
    private Double averageScore;

    @CreationTimestamp
    @Column(name = "analysis_date", updatable = false)
    private LocalDateTime analysisDate;

    public AnalysisRecord() {}

    public AnalysisRecord(Long id, FeedbackUpload upload, User user, Integer totalFeedback,
                          Integer positiveCount, Integer negativeCount, Integer neutralCount,
                          Double averageScore, LocalDateTime analysisDate) {
        this.id = id;
        this.upload = upload;
        this.user = user;
        this.totalFeedback = totalFeedback;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.averageScore = averageScore;
        this.analysisDate = analysisDate;
    }

    public static AnalysisRecordBuilder builder() {
        return new AnalysisRecordBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeedbackUpload getUpload() { return upload; }
    public void setUpload(FeedbackUpload upload) { this.upload = upload; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getTotalFeedback() { return totalFeedback; }
    public void setTotalFeedback(Integer totalFeedback) { this.totalFeedback = totalFeedback; }

    public Integer getPositiveCount() { return positiveCount; }
    public void setPositiveCount(Integer positiveCount) { this.positiveCount = positiveCount; }

    public Integer getNegativeCount() { return negativeCount; }
    public void setNegativeCount(Integer negativeCount) { this.negativeCount = negativeCount; }

    public Integer getNeutralCount() { return neutralCount; }
    public void setNeutralCount(Integer neutralCount) { this.neutralCount = neutralCount; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public LocalDateTime getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; }

    public static class AnalysisRecordBuilder {
        private Long id;
        private FeedbackUpload upload;
        private User user;
        private Integer totalFeedback;
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer neutralCount;
        private Double averageScore;
        private LocalDateTime analysisDate;

        public AnalysisRecordBuilder id(Long id) { this.id = id; return this; }
        public AnalysisRecordBuilder upload(FeedbackUpload upload) { this.upload = upload; return this; }
        public AnalysisRecordBuilder user(User user) { this.user = user; return this; }
        public AnalysisRecordBuilder totalFeedback(Integer totalFeedback) { this.totalFeedback = totalFeedback; return this; }
        public AnalysisRecordBuilder positiveCount(Integer positiveCount) { this.positiveCount = positiveCount; return this; }
        public AnalysisRecordBuilder negativeCount(Integer negativeCount) { this.negativeCount = negativeCount; return this; }
        public AnalysisRecordBuilder neutralCount(Integer neutralCount) { this.neutralCount = neutralCount; return this; }
        public AnalysisRecordBuilder averageScore(Double averageScore) { this.averageScore = averageScore; return this; }
        public AnalysisRecordBuilder analysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; return this; }

        public AnalysisRecord build() {
            return new AnalysisRecord(id, upload, user, totalFeedback, positiveCount, negativeCount, neutralCount, averageScore, analysisDate);
        }
    }
}
