package com.example.feedback.service;

import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.SentimentResult;
import com.example.feedback.entity.SentimentType;
import com.example.feedback.entity.Suggestion;
import com.example.feedback.repository.AnalysisRecordRepository;
import com.example.feedback.repository.SuggestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final AnalysisRecordRepository analysisRecordRepository;

    public SuggestionService(SuggestionRepository suggestionRepository,
                             AnalysisRecordRepository analysisRecordRepository) {
        this.suggestionRepository = suggestionRepository;
        this.analysisRecordRepository = analysisRecordRepository;
    }

    private static class CategoryRule {
        String category;
        List<String> keywords;
        String suggestionText;
        String recommendedAction;

        CategoryRule(String category, List<String> keywords, String suggestionText, String recommendedAction) {
            this.category = category;
            this.keywords = keywords;
            this.suggestionText = suggestionText;
            this.recommendedAction = recommendedAction;
        }
    }

    private final List<CategoryRule> rules = Arrays.asList(
            new CategoryRule(
                    "Teaching Pace",
                    Arrays.asList("fast", "quick", "pace", "too fast", "rushing", "speed", "slow down"),
                    "Consider reducing the teaching pace and providing additional explanations.",
                    "Reduce teaching speed and allow more time for questions."
            ),
            new CategoryRule(
                    "Subject Difficulty",
                    Arrays.asList("difficult", "confusing", "unclear", "hard", "understand", "complex", "concept", "explain"),
                    "Provide simpler explanations, examples, and additional learning materials.",
                    "Use more real-world examples and simplified explanations."
            ),
            new CategoryRule(
                    "Practical Classes",
                    Arrays.asList("practical", "lab", "hands-on", "demonstration", "experiment", "practice"),
                    "Increase practical sessions and hands-on demonstrations.",
                    "Increase laboratory sessions and hands-on activities."
            ),
            new CategoryRule(
                    "Learning Materials",
                    Arrays.asList("notes", "materials", "study material", "slide", "slides", "textbook", "reference", "resource"),
                    "Provide additional study materials, notes, and reference resources.",
                    "Provide additional notes and reference materials."
            ),
            new CategoryRule(
                    "Assignment Workload",
                    Arrays.asList("assignment", "workload", "too much", "homework", "task", "deadline", "pressure"),
                    "Review the assignment workload and distribute tasks more evenly.",
                    "Distribute assignments evenly across the semester."
            ),
            new CategoryRule(
                    "Communication",
                    Arrays.asList("communication", "talk", "listen", "approachable", "express", "doubt", "discussions"),
                    "Improve communication channels and encourage open discussion with students.",
                    "Conduct clear interactive explanation sessions and open office hours."
            ),
            new CategoryRule(
                    "Examination",
                    Arrays.asList("exam", "examination", "test", "grading", "marks", "quiz", "paper", "evaluat"),
                    "Review examination structures and provide clearer evaluation rubrics.",
                    "Provide mock tests and clear evaluation criteria."
            ),
            new CategoryRule(
                    "Student Engagement",
                    Arrays.asList("engagement", "interactive", "boring", "participation", "dull", "interest", "interact"),
                    "Encourage active student engagement and interactive class activities.",
                    "Incorporate quizzes, Q&A sessions, and group activities."
            ),
            new CategoryRule(
                    "Course Content",
                    Arrays.asList("content", "syllabus", "curriculum", "outdated", "topic", "subject content"),
                    "Update course content with modern, real-world industry examples.",
                    "Refresh syllabus with modern practical topics."
            ),
            new CategoryRule(
                    "Classroom Environment",
                    Arrays.asList("classroom", "environment", "room", "projector", "bench", "seat", "noise", "ac", "equipment"),
                    "Improve classroom facilities and learning environment.",
                    "Upgrade classroom infrastructure and seating arrangements."
            ),
            new CategoryRule(
                    "Teaching Method",
                    Arrays.asList("method", "teaching style", "lecture", "monotonous", "technique", "approach"),
                    "Adopt interactive teaching methodologies to enhance student understanding.",
                    "Implement multi-modal and visual teaching techniques."
            )
    );

    @Transactional
    public List<Suggestion> generateSuggestions(AnalysisRecord record, List<Feedback> feedbacks, List<SentimentResult> results) {
        // Delete any existing suggestions for this record to allow regeneration
        suggestionRepository.deleteByAnalysisRecord(record);

        Map<Long, SentimentResult> resultMap = results.stream()
                .collect(Collectors.toMap(r -> r.getFeedback().getId(), r -> r, (r1, r2) -> r1));

        Map<CategoryRule, List<Feedback>> categoryMatches = new LinkedHashMap<>();
        Map<CategoryRule, Long> categoryNegativeCounts = new HashMap<>();

        for (CategoryRule rule : rules) {
            List<Feedback> matched = new ArrayList<>();
            long negCount = 0;

            for (Feedback fb : feedbacks) {
                String text = fb.getFeedbackText().toLowerCase();
                boolean matches = rule.keywords.stream().anyMatch(text::contains);

                if (matches) {
                    matched.add(fb);
                    SentimentResult sr = resultMap.get(fb.getId());
                    if (sr != null && sr.getSentiment() == SentimentType.NEGATIVE) {
                        negCount++;
                    }
                }
            }

            if (!matched.isEmpty()) {
                categoryMatches.put(rule, matched);
                categoryNegativeCounts.put(rule, negCount);
            }
        }

        // If no specific keyword matches were found, but there are negative feedback items, add general suggestions
        if (categoryMatches.isEmpty() && record.getNegativeCount() > 0) {
            for (CategoryRule rule : rules.subList(0, Math.min(3, rules.size()))) {
                categoryMatches.put(rule, feedbacks.subList(0, Math.min(feedbacks.size(), 2)));
                categoryNegativeCounts.put(rule, 1L);
            }
        }

        List<Suggestion> generatedList = new ArrayList<>();

        for (Map.Entry<CategoryRule, List<Feedback>> entry : categoryMatches.entrySet()) {
            CategoryRule rule = entry.getKey();
            List<Feedback> matchedFeedbacks = entry.getValue();
            long negCount = categoryNegativeCounts.getOrDefault(rule, 0L);
            int totalMatched = matchedFeedbacks.size();

            double negRatio = totalMatched > 0 ? (double) negCount / totalMatched : 0.0;

            String priority = "LOW";
            if (negCount >= 3 || (totalMatched >= 3 && negRatio >= 0.4)) {
                priority = "HIGH";
            } else if (negCount >= 1 || totalMatched >= 2) {
                priority = "MEDIUM";
            }

            Suggestion suggestion = Suggestion.builder()
                    .analysisRecord(record)
                    .category(rule.category)
                    .suggestionText(rule.suggestionText)
                    .priority(priority)
                    .relatedFeedbackCount(totalMatched)
                    .status("NEW")
                    .actionPlan(rule.recommendedAction)
                    .responsiblePerson("Faculty")
                    .targetDate("")
                    .remarks("")
                    .build();

            generatedList.add(suggestionRepository.save(suggestion));
        }

        return generatedList;
    }

    public List<Suggestion> getSuggestionsForRecord(AnalysisRecord record) {
        return suggestionRepository.findByAnalysisRecordOrderByPriorityAscCreatedAtDesc(record);
    }

    public List<Suggestion> getSuggestionsForRecordId(Long recordId) {
        return suggestionRepository.findByAnalysisRecordId(recordId);
    }

    public List<Suggestion> getAllSuggestions() {
        return suggestionRepository.findAllByOrderByCreatedAtDesc();
    }

    public String getOverallRecommendation(AnalysisRecord record, List<Suggestion> suggestions) {
        if (record == null || record.getTotalFeedback() == 0) {
            return "No feedback data available for recommendation analysis.";
        }
        if (suggestions.isEmpty()) {
            return String.format("Based on the analysis of %d student responses, overall feedback is balanced with no major specific issues highlighted.", record.getTotalFeedback());
        }

        List<String> topCategories = suggestions.stream()
                .sorted((s1, s2) -> Integer.compare(priorityWeight(s1.getPriority()), priorityWeight(s2.getPriority())))
                .map(Suggestion::getCategory)
                .distinct()
                .limit(3)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        String categoriesStr = String.join(", ", topCategories);
        return String.format("Based on the analysis of %d student responses, the major improvement areas are %s.", record.getTotalFeedback(), categoriesStr);
    }

    public List<String> getFutureImprovementSummary(List<Suggestion> suggestions) {
        List<String> summaryList = new ArrayList<>();
        int count = 1;
        for (Suggestion s : suggestions) {
            if (count > 5) break;
            summaryList.add(count + ". " + s.getActionPlan());
            count++;
        }
        if (summaryList.isEmpty()) {
            summaryList.add("1. Maintain current teaching quality and encourage continuous student feedback.");
        }
        return summaryList;
    }

    private int priorityWeight(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) return 1;
        if ("MEDIUM".equalsIgnoreCase(priority)) return 2;
        return 3;
    }

    @Transactional
    public Suggestion updateSuggestionStatus(Long suggestionId, String status) {
        Optional<Suggestion> opt = suggestionRepository.findById(suggestionId);
        if (opt.isPresent()) {
            Suggestion suggestion = opt.get();
            suggestion.setStatus(status);
            return suggestionRepository.save(suggestion);
        }
        return null;
    }

    @Transactional
    public Suggestion updateActionPlan(Long suggestionId, String actionPlan, String responsiblePerson, String targetDate, String status, String remarks) {
        Optional<Suggestion> opt = suggestionRepository.findById(suggestionId);
        if (opt.isPresent()) {
            Suggestion suggestion = opt.get();
            if (actionPlan != null) suggestion.setActionPlan(actionPlan);
            if (responsiblePerson != null) suggestion.setResponsiblePerson(responsiblePerson);
            if (targetDate != null) suggestion.setTargetDate(targetDate);
            if (status != null) suggestion.setStatus(status);
            if (remarks != null) suggestion.setRemarks(remarks);
            return suggestionRepository.save(suggestion);
        }
        return null;
    }

    @Transactional
    public void deleteSuggestion(Long suggestionId) {
        suggestionRepository.deleteById(suggestionId);
    }

    @Transactional
    public Suggestion addCustomSuggestion(Long recordId, String category, String suggestionText, String priority, String status, String actionPlan, String responsiblePerson, String targetDate, String remarks) {
        Optional<AnalysisRecord> recordOpt = analysisRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Analysis Record not found for ID: " + recordId);
        }

        Suggestion suggestion = Suggestion.builder()
                .analysisRecord(recordOpt.get())
                .category(category != null ? category : "General")
                .suggestionText(suggestionText != null ? suggestionText : "")
                .priority(priority != null ? priority : "MEDIUM")
                .relatedFeedbackCount(1)
                .status(status != null ? status : "NEW")
                .actionPlan(actionPlan != null ? actionPlan : "")
                .responsiblePerson(responsiblePerson != null ? responsiblePerson : "Faculty")
                .targetDate(targetDate != null ? targetDate : "")
                .remarks(remarks != null ? remarks : "")
                .build();

        return suggestionRepository.save(suggestion);
    }

    public List<String> getNegativeKeywordsForRecord(List<Feedback> feedbacks, List<SentimentResult> results) {
        Map<Long, SentimentResult> resultMap = results.stream()
                .collect(Collectors.toMap(r -> r.getFeedback().getId(), r -> r, (r1, r2) -> r1));

        Map<String, Integer> freq = new HashMap<>();
        List<String> stopWords = Arrays.asList("the", "and", "is", "in", "to", "of", "it", "this", "for", "with", "on", "was", "are", "be", "very", "too", "more", "a", "an", "so", "should", "given", "need", "should", "not");

        for (Feedback fb : feedbacks) {
            SentimentResult sr = resultMap.get(fb.getId());
            if (sr != null && sr.getSentiment() == SentimentType.NEGATIVE) {
                String[] words = fb.getFeedbackText().toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
                for (String w : words) {
                    if (w.length() > 3 && !stopWords.contains(w)) {
                        freq.put(w, freq.getOrDefault(w, 0) + 1);
                    }
                }
            }
        }

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public long countByPriority(String priority) {
        return suggestionRepository.countByPriority(priority);
    }

    public long countByStatus(String status) {
        return suggestionRepository.countByStatus(status);
    }
}
