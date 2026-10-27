package com.example.feedback.service;

import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.FeedbackUpload;
import com.example.feedback.repository.AnalysisRecordRepository;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.FeedbackUploadRepository;
import com.example.feedback.repository.SentimentResultRepository;
import com.example.feedback.repository.SuggestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final FeedbackUploadRepository uploadRepository;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final SentimentResultRepository resultRepository;
    private final SuggestionRepository suggestionRepository;
    private final FeedbackRepository feedbackRepository;

    public DashboardService(FeedbackUploadRepository uploadRepository,
                            AnalysisRecordRepository analysisRecordRepository,
                            SentimentResultRepository resultRepository,
                            SuggestionRepository suggestionRepository,
                            FeedbackRepository feedbackRepository) {
        this.uploadRepository = uploadRepository;
        this.analysisRecordRepository = analysisRecordRepository;
        this.resultRepository = resultRepository;
        this.suggestionRepository = suggestionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public long getTotalUploads() {
        return uploadRepository.count();
    }

    public List<AnalysisRecord> getAllAnalysisRecords() {
        return analysisRecordRepository.findAllByOrderByAnalysisDateDesc();
    }

    public long getTotalFeedbackCount() {
        return analysisRecordRepository.findAll().stream()
                .mapToLong(r -> r.getTotalFeedback() != null ? r.getTotalFeedback() : 0)
                .sum();
    }

    public long getTotalPositive() {
        return analysisRecordRepository.findAll().stream()
                .mapToLong(r -> r.getPositiveCount() != null ? r.getPositiveCount() : 0)
                .sum();
    }

    public long getTotalNegative() {
        return analysisRecordRepository.findAll().stream()
                .mapToLong(r -> r.getNegativeCount() != null ? r.getNegativeCount() : 0)
                .sum();
    }

    public long getTotalNeutral() {
        return analysisRecordRepository.findAll().stream()
                .mapToLong(r -> r.getNeutralCount() != null ? r.getNeutralCount() : 0)
                .sum();
    }

    /**
     * Clears ALL system analysis data in the correct FK-safe order:
     * suggestions → sentimentResults → analysisRecords → feedbacks → uploads
     */
    @Transactional
    public void clearOldRecords() {
        suggestionRepository.deleteAll();
        resultRepository.deleteAll();
        analysisRecordRepository.deleteAll();
        feedbackRepository.deleteAll();
        uploadRepository.deleteAll();
    }
}
