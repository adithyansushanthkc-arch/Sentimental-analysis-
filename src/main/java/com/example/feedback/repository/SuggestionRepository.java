package com.example.feedback.repository;

import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByAnalysisRecordOrderByPriorityAscCreatedAtDesc(AnalysisRecord record);

    List<Suggestion> findByAnalysisRecordId(Long analysisRecordId);

    List<Suggestion> findAllByOrderByCreatedAtDesc();

    void deleteByAnalysisRecord(AnalysisRecord record);

    long countByPriority(String priority);

    long countByStatus(String status);
}
