package com.example.feedback.service;

import com.example.feedback.entity.SentimentDictionary;
import com.example.feedback.entity.SentimentType;
import com.example.feedback.repository.SentimentDictionaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DictionaryService {

    private final SentimentDictionaryRepository dictionaryRepository;

    public DictionaryService(SentimentDictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public SentimentDictionary saveWord(SentimentDictionary dictionary) {
        dictionary.setWord(dictionary.getWord().toLowerCase().trim());
        return dictionaryRepository.save(dictionary);
    }

    public SentimentDictionary updateWord(Long id, String word, SentimentType sentimentType, Integer weight, String status) {
        Optional<SentimentDictionary> existingOpt = dictionaryRepository.findById(id);
        if (existingOpt.isPresent()) {
            SentimentDictionary dict = existingOpt.get();
            dict.setWord(word.toLowerCase().trim());
            dict.setSentimentType(sentimentType);
            dict.setWeight(weight);
            if (status != null && !status.isEmpty()) {
                dict.setStatus(status);
            }
            return dictionaryRepository.save(dict);
        }
        throw new IllegalArgumentException("Word not found with ID: " + id);
    }

    public List<SentimentDictionary> getAllWords() {
        return dictionaryRepository.findAll();
    }

    public List<SentimentDictionary> getActiveWords() {
        return dictionaryRepository.findByStatus("Active");
    }

    public Optional<SentimentDictionary> getWordById(Long id) {
        return dictionaryRepository.findById(id);
    }

    public void deleteWord(Long id) {
        dictionaryRepository.deleteById(id);
    }

    public int getPositiveWordCount() {
        return dictionaryRepository.countBySentimentType(SentimentType.POSITIVE);
    }

    public int getNegativeWordCount() {
        return dictionaryRepository.countBySentimentType(SentimentType.NEGATIVE);
    }
    
    public void deleteAllWords() {
        dictionaryRepository.deleteAll();
    }
}
