package com.example.feedback.service;

import com.example.feedback.entity.*;
import com.example.feedback.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SentimentAnalysisService {

    private final DictionaryService dictionaryService;
    private final SentimentResultRepository resultRepository;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final SuggestionService suggestionService;

    public SentimentAnalysisService(DictionaryService dictionaryService,
                                    SentimentResultRepository resultRepository,
                                    AnalysisRecordRepository analysisRecordRepository,
                                    SuggestionService suggestionService) {
        this.dictionaryService = dictionaryService;
        this.resultRepository = resultRepository;
        this.analysisRecordRepository = analysisRecordRepository;
        this.suggestionService = suggestionService;
    }

    @Transactional
    public void analyzeFeedback(FeedbackUpload upload, List<Feedback> feedbacks) {
        List<SentimentDictionary> activeWords = dictionaryService.getActiveWords();

        int totalPositive = 0;
        int totalNegative = 0;
        int totalNeutral = 0;
        int totalScore = 0;
        List<SentimentResult> sentimentResults = new ArrayList<>();

        for (Feedback feedback : feedbacks) {
            String text = normalizeText(feedback.getFeedbackText());
            String[] words = text.split("\\s+");

            int score = 0;
            int posCount = 0;
            int negCount = 0;

            for (String word : words) {
                SentimentDictionary dictWord = activeWords.stream()
                        .filter(dw -> dw.getWord().equals(word))
                        .findFirst()
                        .orElse(null);

                if (dictWord != null) {
                    score += dictWord.getWeight();
                    if (dictWord.getSentimentType() == SentimentType.POSITIVE) {
                        posCount++;
                    } else if (dictWord.getSentimentType() == SentimentType.NEGATIVE) {
                        negCount++;
                    }
                }
            }

            SentimentType type = SentimentType.NEUTRAL;
            if (score > 0) {
                type = SentimentType.POSITIVE;
                totalPositive++;
            } else if (score < 0) {
                type = SentimentType.NEGATIVE;
                totalNegative++;
            } else {
                totalNeutral++;
            }
            totalScore += score;

            SentimentResult result = SentimentResult.builder()
                    .feedback(feedback)
                    .sentiment(type)
                    .sentimentScore(score)
                    .positiveWordCount(posCount)
                    .negativeWordCount(negCount)
                    .build();

            resultRepository.save(result);
            sentimentResults.add(result);
        }

        AnalysisRecord record = AnalysisRecord.builder()
                .upload(upload)
                .user(upload.getUploadedBy())
                .totalFeedback(feedbacks.size())
                .positiveCount(totalPositive)
                .negativeCount(totalNegative)
                .neutralCount(totalNeutral)
                .averageScore(feedbacks.size() > 0 ? (double) totalScore / feedbacks.size() : 0.0)
                .build();

        record = analysisRecordRepository.save(record);

        // Generate Future Suggestions & Recommendations automatically
        suggestionService.generateSuggestions(record, feedbacks, sentimentResults);
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        // Convert to lowercase and remove punctuation
        return text.toLowerCase().replaceAll("[^a-z\\s]", " ").trim();
    }
}
