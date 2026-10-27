package com.example.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisRecord analysisRecord;

    @Column(nullable = false)
    private String category;

    @Column(name = "suggestion_text", columnDefinition = "TEXT", nullable = false)
    private String suggestionText;

    @Column(nullable = false)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "related_feedback_count")
    private Integer relatedFeedbackCount;

    @Column(nullable = false)
    private String status; // NEW, REVIEWED, IN PROGRESS, IMPLEMENTED

    @Column(name = "action_plan", columnDefinition = "TEXT")
    private String actionPlan;

    @Column(name = "responsible_person")
    private String responsiblePerson;

    @Column(name = "target_date")
    private String targetDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Suggestion() {}

    public Suggestion(Long id, AnalysisRecord analysisRecord, String category, String suggestionText,
                      String priority, Integer relatedFeedbackCount, String status, String actionPlan,
                      String responsiblePerson, String targetDate, String remarks,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.analysisRecord = analysisRecord;
        this.category = category;
        this.suggestionText = suggestionText;
        this.priority = priority;
        this.relatedFeedbackCount = relatedFeedbackCount;
        this.status = status;
        this.actionPlan = actionPlan;
        this.responsiblePerson = responsiblePerson;
        this.targetDate = targetDate;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SuggestionBuilder builder() {
        return new SuggestionBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisRecord getAnalysisRecord() {
        return analysisRecord;
    }

    public void setAnalysisRecord(AnalysisRecord analysisRecord) {
        this.analysisRecord = analysisRecord;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSuggestionText() {
        return suggestionText;
    }

    public void setSuggestionText(String suggestionText) {
        this.suggestionText = suggestionText;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getRelatedFeedbackCount() {
        return relatedFeedbackCount;
    }

    public void setRelatedFeedbackCount(Integer relatedFeedbackCount) {
        this.relatedFeedbackCount = relatedFeedbackCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class SuggestionBuilder {
        private Long id;
        private AnalysisRecord analysisRecord;
        private String category;
        private String suggestionText;
        private String priority;
        private Integer relatedFeedbackCount;
        private String status;
        private String actionPlan;
        private String responsiblePerson;
        private String targetDate;
        private String remarks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SuggestionBuilder id(Long id) { this.id = id; return this; }
        public SuggestionBuilder analysisRecord(AnalysisRecord analysisRecord) { this.analysisRecord = analysisRecord; return this; }
        public SuggestionBuilder category(String category) { this.category = category; return this; }
        public SuggestionBuilder suggestionText(String suggestionText) { this.suggestionText = suggestionText; return this; }
        public SuggestionBuilder priority(String priority) { this.priority = priority; return this; }
        public SuggestionBuilder relatedFeedbackCount(Integer relatedFeedbackCount) { this.relatedFeedbackCount = relatedFeedbackCount; return this; }
        public SuggestionBuilder status(String status) { this.status = status; return this; }
        public SuggestionBuilder actionPlan(String actionPlan) { this.actionPlan = actionPlan; return this; }
        public SuggestionBuilder responsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; return this; }
        public SuggestionBuilder targetDate(String targetDate) { this.targetDate = targetDate; return this; }
        public SuggestionBuilder remarks(String remarks) { this.remarks = remarks; return this; }
        public SuggestionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SuggestionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Suggestion build() {
            return new Suggestion(id, analysisRecord, category, suggestionText, priority, relatedFeedbackCount, status, actionPlan, responsiblePerson, targetDate, remarks, createdAt, updatedAt);
        }
    }
}
