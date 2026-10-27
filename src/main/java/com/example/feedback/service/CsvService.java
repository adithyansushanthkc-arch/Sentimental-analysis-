package com.example.feedback.service;

import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.FeedbackUpload;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    public List<Feedback> parseCsvFile(MultipartFile file, FeedbackUpload upload) throws Exception {
        List<Feedback> feedbackList = new ArrayList<>();
        
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build(); // Skip header
            List<String[]> records = csvReader.readAll();
            
            for (String[] record : records) {
                if (record.length >= 4) {
                    Feedback feedback = new Feedback();
                    feedback.setUpload(upload);
                    feedback.setStudentId(record[0]);
                    feedback.setFaculty(record[1]);
                    feedback.setSubject(record[2]);
                    feedback.setFeedbackText(record[3]);
                    
                    if(feedback.getFeedbackText() != null && !feedback.getFeedbackText().trim().isEmpty()){
                       feedbackList.add(feedback);
                    }
                }
            }
        }
        return feedbackList;
    }
}
