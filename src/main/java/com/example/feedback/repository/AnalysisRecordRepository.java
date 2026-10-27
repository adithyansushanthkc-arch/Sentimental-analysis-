package com.example.feedback.repository;

import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {
    List<AnalysisRecord> findByUserOrderByAnalysisDateDesc(User user);
    List<AnalysisRecord> findAllByOrderByAnalysisDateDesc();
}
