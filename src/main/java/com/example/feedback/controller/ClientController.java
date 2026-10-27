package com.example.feedback.controller;

import com.example.feedback.entity.ActivityType;
import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.FeedbackUpload;
import com.example.feedback.entity.User;
import com.example.feedback.repository.AnalysisRecordRepository;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.FeedbackUploadRepository;
import com.example.feedback.repository.SentimentResultRepository;
import com.example.feedback.service.CsvService;
import com.example.feedback.service.ReportService;
import com.example.feedback.service.SentimentAnalysisService;
import com.example.feedback.service.UserActivityService;
import com.example.feedback.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import com.example.feedback.entity.Suggestion;
import com.example.feedback.service.SuggestionService;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final CsvService csvService;
    private final SentimentAnalysisService analysisService;
    private final FeedbackUploadRepository uploadRepository;
    private final FeedbackRepository feedbackRepository;
    private final SentimentResultRepository resultRepository;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final UserService userService;
    private final ReportService reportService;
    private final SuggestionService suggestionService;
    private final UserActivityService userActivityService;

    public ClientController(CsvService csvService,
                            SentimentAnalysisService analysisService,
                            FeedbackUploadRepository uploadRepository,
                            FeedbackRepository feedbackRepository,
                            SentimentResultRepository resultRepository,
                            AnalysisRecordRepository analysisRecordRepository,
                            UserService userService,
                            ReportService reportService,
                            SuggestionService suggestionService,
                            UserActivityService userActivityService) {
        this.csvService = csvService;
        this.analysisService = analysisService;
        this.uploadRepository = uploadRepository;
        this.feedbackRepository = feedbackRepository;
        this.resultRepository = resultRepository;
        this.analysisRecordRepository = analysisRecordRepository;
        this.userService = userService;
        this.reportService = reportService;
        this.suggestionService = suggestionService;
        this.userActivityService = userActivityService;
    }

    private User getLoggedInUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = getLoggedInUser(auth);
        List<AnalysisRecord> records = analysisRecordRepository.findByUserOrderByAnalysisDateDesc(user);
        
        long totalFeedback = records.stream().mapToLong(r -> r.getTotalFeedback() != null ? r.getTotalFeedback() : 0).sum();
        long positiveCount = records.stream().mapToLong(r -> r.getPositiveCount() != null ? r.getPositiveCount() : 0).sum();
        long negativeCount = records.stream().mapToLong(r -> r.getNegativeCount() != null ? r.getNegativeCount() : 0).sum();
        long neutralCount = records.stream().mapToLong(r -> r.getNeutralCount() != null ? r.getNeutralCount() : 0).sum();

        model.addAttribute("totalFeedback", totalFeedback);
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("neutralCount", neutralCount);
        model.addAttribute("recentRecords", records.size() > 5 ? records.subList(0, 5) : records);
        
        return "client/dashboard";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "client/upload";
    }

    @GetMapping("/sample-csv")
    public ResponseEntity<byte[]> downloadSampleCsv() {
        String csvContent = "Student_ID,Faculty,Subject,Feedback\n" +
                "S101,Dr. John Smith,Data Structures,The lectures are very clear and helpful.\n" +
                "S102,Dr. Jane Doe,Database Systems,Explanations are confusing and the pace is too fast.\n" +
                "S103,Dr. John Smith,Data Structures,Excellent teaching style and great lab guidance.\n" +
                "S104,Dr. Alan Turing,Algorithms,The assignments are difficult and hard to follow.\n" +
                "S105,Dr. Jane Doe,Database Systems,Good course content and effective presentation.\n";

        byte[] bytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "sample_student_feedback.csv");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Authentication auth, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a CSV file to upload.");
            return "redirect:/client/upload";
        }

        try {
            User user = getLoggedInUser(auth);
            FeedbackUpload upload = new FeedbackUpload();
            upload.setFileName(file.getOriginalFilename());
            upload.setUploadedBy(user);
            upload.setStatus("Uploaded");
            uploadRepository.save(upload);

            List<Feedback> feedbacks = csvService.parseCsvFile(file, upload);
            if(feedbacks.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "CSV file is empty or missing required columns.");
                return "redirect:/client/upload";
            }
            
            feedbackRepository.saveAll(feedbacks);
            upload.setTotalRecords(feedbacks.size());
            uploadRepository.save(upload);

            userActivityService.logActivity(user, ActivityType.FEEDBACK_UPLOAD, "Uploaded feedback file: " + file.getOriginalFilename() + " (" + feedbacks.size() + " records)");

            redirectAttributes.addFlashAttribute("successMessage", "File uploaded successfully. Records found: " + feedbacks.size());
            return "redirect:/client/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error parsing CSV file: " + e.getMessage());
            return "redirect:/client/upload";
        }
    }

    @PostMapping("/analyze/{uploadId}")
    public String performAnalysis(@PathVariable Long uploadId, Authentication auth, RedirectAttributes redirectAttributes) {
        Optional<FeedbackUpload> uploadOpt = uploadRepository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload not found.");
            return "redirect:/client/history";
        }

        FeedbackUpload upload = uploadOpt.get();
        if ("Analyzed".equals(upload.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This file is already analyzed.");
            return "redirect:/client/history";
        }

        try {
            User user = getLoggedInUser(auth);
            List<Feedback> feedbacks = feedbackRepository.findByUpload(upload);
            analysisService.analyzeFeedback(upload, feedbacks);
            upload.setStatus("Analyzed");
            uploadRepository.save(upload);
            
            userActivityService.logActivity(user, ActivityType.SENTIMENT_ANALYSIS, "Executed sentiment analysis on upload #" + uploadId + " (" + upload.getFileName() + ")");

            redirectAttributes.addFlashAttribute("successMessage", "Sentiment analysis completed successfully.");
            return "redirect:/client/results/" + uploadId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during analysis: " + e.getMessage());
            return "redirect:/client/history";
        }
    }

    @GetMapping("/results/{uploadId}")
    public String viewResults(@PathVariable Long uploadId, Model model, RedirectAttributes redirectAttributes) {
        Optional<FeedbackUpload> uploadOpt = uploadRepository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Upload not found.");
            return "redirect:/client/history";
        }
        
        model.addAttribute("upload", uploadOpt.get());
        model.addAttribute("results", resultRepository.findByUpload(uploadOpt.get()));
        
        Optional<AnalysisRecord> record = analysisRecordRepository.findAll().stream().filter(r -> r.getUpload().getId().equals(uploadId)).findFirst();
        record.ifPresent(analysisRecord -> model.addAttribute("record", analysisRecord));
        
        return "client/results";
    }

    @GetMapping("/reports")
    public String reportsPage(Model model, Authentication auth) {
        User user = getLoggedInUser(auth);
        model.addAttribute("records", analysisRecordRepository.findByUserOrderByAnalysisDateDesc(user));
        return "client/reports";
    }

    @GetMapping("/reports/download/{recordId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long recordId, Authentication auth) {
        Optional<AnalysisRecord> recordOpt = analysisRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = getLoggedInUser(auth);
        AnalysisRecord record = recordOpt.get();
        byte[] pdfBytes = reportService.generatePdfReport(record, resultRepository.findByUpload(record.getUpload()));
        
        userActivityService.logActivity(user, ActivityType.REPORT_GENERATED, "Downloaded PDF Report for record #" + recordId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Sentiment_Report_" + record.getId() + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/history")
    public String historyPage(Model model, Authentication auth) {
        User user = getLoggedInUser(auth);
        model.addAttribute("uploads", uploadRepository.findByUploadedByOrderByUploadDateDesc(user));
        return "client/history";
    }

    @GetMapping("/suggestions")
    public String suggestionsPage(@RequestParam(value = "recordId", required = false) Long recordId, Model model, Authentication auth) {
        User user = getLoggedInUser(auth);
        List<AnalysisRecord> records = analysisRecordRepository.findByUserOrderByAnalysisDateDesc(user);

        if (records.isEmpty()) {
            model.addAttribute("noData", true);
            return "client/suggestions";
        }

        AnalysisRecord selectedRecord = null;
        if (recordId != null) {
            selectedRecord = records.stream().filter(r -> r.getId().equals(recordId)).findFirst().orElse(records.get(0));
        } else {
            selectedRecord = records.get(0);
        }

        List<Suggestion> suggestions = suggestionService.getSuggestionsForRecord(selectedRecord);
        String overallRecommendation = suggestionService.getOverallRecommendation(selectedRecord, suggestions);
        List<String> futureImprovementSummary = suggestionService.getFutureImprovementSummary(suggestions);

        model.addAttribute("records", records);
        model.addAttribute("selectedRecord", selectedRecord);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("overallRecommendation", overallRecommendation);
        model.addAttribute("futureImprovementSummary", futureImprovementSummary);

        return "client/suggestions";
    }

    @PostMapping("/suggestions/action-plan/update")
    public String updateActionPlan(
            @RequestParam("suggestionId") Long suggestionId,
            @RequestParam("recordId") Long recordId,
            @RequestParam(value = "actionPlan", required = false) String actionPlan,
            @RequestParam(value = "responsiblePerson", required = false) String responsiblePerson,
            @RequestParam(value = "targetDate", required = false) String targetDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "remarks", required = false) String remarks,
            RedirectAttributes redirectAttributes) {

        try {
            suggestionService.updateActionPlan(suggestionId, actionPlan, responsiblePerson, targetDate, status, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Action Plan updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating Action Plan: " + e.getMessage());
        }

        return "redirect:/client/suggestions?recordId=" + recordId;
    }
}
