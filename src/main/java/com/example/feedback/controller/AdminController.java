package com.example.feedback.controller;

import com.example.feedback.entity.ActivityType;
import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.FeedbackUpload;
import com.example.feedback.entity.Role;
import com.example.feedback.entity.SentimentDictionary;
import com.example.feedback.entity.SentimentResult;
import com.example.feedback.entity.SentimentType;
import com.example.feedback.entity.Suggestion;
import com.example.feedback.entity.User;
import com.example.feedback.entity.UserActivity;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.FeedbackUploadRepository;
import com.example.feedback.repository.SentimentDictionaryRepository;
import com.example.feedback.repository.SentimentResultRepository;
import com.example.feedback.repository.SuggestionRepository;
import com.example.feedback.repository.UserActivityRepository;
import com.example.feedback.repository.UserRepository;
import com.example.feedback.service.DashboardService;
import com.example.feedback.service.DictionaryService;
import com.example.feedback.service.SuggestionService;
import com.example.feedback.service.UserActivityService;
import com.example.feedback.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final DictionaryService dictionaryService;
    private final DashboardService dashboardService;
    private final SuggestionService suggestionService;
    private final UserActivityService userActivityService;
    private final FeedbackRepository feedbackRepository;
    private final SentimentResultRepository resultRepository;
    private final FeedbackUploadRepository uploadRepository;
    private final UserRepository userRepository;
    private final SentimentDictionaryRepository sentimentDictionaryRepository;
    private final SuggestionRepository suggestionRepository;
    private final UserActivityRepository userActivityRepository;

    public AdminController(UserService userService,
                           DictionaryService dictionaryService,
                           DashboardService dashboardService,
                           SuggestionService suggestionService,
                           UserActivityService userActivityService,
                           FeedbackRepository feedbackRepository,
                           SentimentResultRepository resultRepository,
                           FeedbackUploadRepository uploadRepository,
                           UserRepository userRepository,
                           SentimentDictionaryRepository sentimentDictionaryRepository,
                           SuggestionRepository suggestionRepository,
                           UserActivityRepository userActivityRepository) {
        this.userService = userService;
        this.dictionaryService = dictionaryService;
        this.dashboardService = dashboardService;
        this.suggestionService = suggestionService;
        this.userActivityService = userActivityService;
        this.feedbackRepository = feedbackRepository;
        this.resultRepository = resultRepository;
        this.uploadRepository = uploadRepository;
        this.userRepository = userRepository;
        this.sentimentDictionaryRepository = sentimentDictionaryRepository;
        this.suggestionRepository = suggestionRepository;
        this.userActivityRepository = userActivityRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Section 1: User Overview
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("facultyUsersCount", userService.getUsersByRoleCount(Role.CLIENT));
        model.addAttribute("adminUsersCount", userService.getUsersByRoleCount(Role.ADMIN));
        model.addAttribute("activeUsers", userService.getActiveUsersCount());

        // Section 2: User Activity
        model.addAttribute("totalLogins", userActivityService.getTotalLogins());
        List<UserActivity> allActivities = userActivityService.getAllActivities();
        List<UserActivity> recentLogins = allActivities.stream()
                .filter(a -> a.getActivityType() == ActivityType.LOGIN)
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentLogins", recentLogins);
        model.addAttribute("recentActivities", userActivityService.getRecentActivities(15));

        // Section 3: Feedback Activity
        model.addAttribute("totalAnalyses", dashboardService.getTotalUploads());
        model.addAttribute("totalFeedback", dashboardService.getTotalFeedbackCount());
        model.addAttribute("positiveCount", dashboardService.getTotalPositive());
        model.addAttribute("negativeCount", dashboardService.getTotalNegative());
        model.addAttribute("neutralCount", dashboardService.getTotalNeutral());

        // Section 4: Chart Visualizations & Faculty Stats
        List<User> facultyList = userService.getUsersByRole(Role.CLIENT);
        Map<String, Long> facultyFeedbackMap = new HashMap<>();
        Map<String, Long> facultyAnalysisMap = new HashMap<>();

        for (User faculty : facultyList) {
            long uploadCount = uploadRepository.findByUploadedByOrderByUploadDateDesc(faculty).size();
            long analysisCount = userActivityService.getUserAnalysisCount(faculty);
            facultyFeedbackMap.put(faculty.getName(), uploadCount);
            facultyAnalysisMap.put(faculty.getName(), analysisCount);
        }

        model.addAttribute("facultyList", facultyList);
        model.addAttribute("facultyFeedbackMap", facultyFeedbackMap);
        model.addAttribute("facultyAnalysisMap", facultyAnalysisMap);
        model.addAttribute("allAnalysisRecords", dashboardService.getAllAnalysisRecords());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "role", required = false) String roleFilter,
                              @RequestParam(value = "status", required = false) String statusFilter,
                              Model model) {

        List<User> users = userService.searchUsers(keyword);

        if (roleFilter != null && !roleFilter.isBlank() && !"ALL".equalsIgnoreCase(roleFilter)) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().name().equalsIgnoreCase(roleFilter))
                    .collect(Collectors.toList());
        }

        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            users = users.stream()
                    .filter(u -> u.getStatus() != null && u.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        }

        Map<Long, Long> userUploadCounts = new HashMap<>();
        Map<Long, Long> userAnalysisCounts = new HashMap<>();
        Map<Long, Long> userActivityCounts = new HashMap<>();

        for (User u : users) {
            userUploadCounts.put(u.getId(), (long) uploadRepository.findByUploadedByOrderByUploadDateDesc(u).size());
            userAnalysisCounts.put(u.getId(), userActivityService.getUserAnalysisCount(u));
            userActivityCounts.put(u.getId(), userActivityService.getUserActivityCount(u));
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentRole", roleFilter != null ? roleFilter : "ALL");
        model.addAttribute("currentStatus", statusFilter != null ? statusFilter : "ALL");
        model.addAttribute("totalFacultyCount", userService.getUsersByRoleCount(Role.CLIENT));
        model.addAttribute("totalAdminCount", userService.getUsersByRoleCount(Role.ADMIN));
        model.addAttribute("userUploadCounts", userUploadCounts);
        model.addAttribute("userAnalysisCounts", userAnalysisCounts);
        model.addAttribute("userActivityCounts", userActivityCounts);

        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            user.setStatus("Active");
            if (user.getRole() == null) {
                user.setRole(Role.CLIENT);
            }
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User account created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/dictionary")
    public String manageDictionary(@RequestParam(value = "filter", required = false) String filter, Model model) {
        List<SentimentDictionary> words = dictionaryService.getAllWords();
        if ("POSITIVE".equalsIgnoreCase(filter)) {
            words = words.stream().filter(w -> w.getSentimentType() == SentimentType.POSITIVE).collect(Collectors.toList());
        } else if ("NEGATIVE".equalsIgnoreCase(filter)) {
            words = words.stream().filter(w -> w.getSentimentType() == SentimentType.NEGATIVE).collect(Collectors.toList());
        }
        model.addAttribute("words", words);
        model.addAttribute("currentFilter", filter != null ? filter.toUpperCase() : "ALL");
        model.addAttribute("positiveCount", dictionaryService.getPositiveWordCount());
        model.addAttribute("negativeCount", dictionaryService.getNegativeWordCount());
        model.addAttribute("totalCount", dictionaryService.getAllWords().size());
        return "admin/dictionary";
    }

    @PostMapping("/dictionary/add")
    public String addWord(@ModelAttribute SentimentDictionary dict, RedirectAttributes redirectAttributes) {
        try {
            dict.setStatus("Active");
            dictionaryService.saveWord(dict);
            redirectAttributes.addFlashAttribute("successMessage", "Word added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding word: " + e.getMessage());
        }
        return "redirect:/admin/dictionary";
    }

    @PostMapping("/dictionary/edit")
    public String editWord(@RequestParam("id") Long id,
                           @RequestParam("word") String word,
                           @RequestParam("sentimentType") SentimentType sentimentType,
                           @RequestParam("weight") Integer weight,
                           @RequestParam(value = "status", required = false, defaultValue = "Active") String status,
                           RedirectAttributes redirectAttributes) {
        try {
            dictionaryService.updateWord(id, word, sentimentType, weight, status);
            redirectAttributes.addFlashAttribute("successMessage", "Word updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating word: " + e.getMessage());
        }
        return "redirect:/admin/dictionary";
    }

    @PostMapping("/dictionary/delete/{id}")
    public String deleteWord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dictionaryService.deleteWord(id);
            redirectAttributes.addFlashAttribute("successMessage", "Word deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting word: " + e.getMessage());
        }
        return "redirect:/admin/dictionary";
    }

    @GetMapping("/analysis-records")
    public String analysisRecords(Model model) {
        model.addAttribute("records", dashboardService.getAllAnalysisRecords());
        return "admin/analysis_records";
    }

    @GetMapping("/system-data")
    public String systemData(Model model) {
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalFeedback", dashboardService.getTotalFeedbackCount());
        model.addAttribute("totalAnalysisRecords", dashboardService.getTotalUploads());
        model.addAttribute("positiveWords", dictionaryService.getPositiveWordCount());
        model.addAttribute("negativeWords", dictionaryService.getNegativeWordCount());
        return "admin/system_data";
    }

    @PostMapping("/system-data/clear")
    public String clearSystemData(RedirectAttributes redirectAttributes) {
        dashboardService.clearOldRecords();
        redirectAttributes.addFlashAttribute("successMessage", "Analysis records cleared successfully.");
        return "redirect:/admin/system-data";
    }

    @PostMapping("/system-data/reset-dict")
    public String resetDictionary(RedirectAttributes redirectAttributes) {
        dictionaryService.deleteAllWords();
        redirectAttributes.addFlashAttribute("successMessage", "Sentiment dictionary reset successfully.");
        return "redirect:/admin/system-data";
    }

    // ===== ADMIN SUGGESTIONS ENDPOINTS =====

    @GetMapping("/suggestions")
    public String adminSuggestions(Model model) {
        List<Suggestion> allSuggestions = suggestionService.getAllSuggestions();

        long highCount = allSuggestions.stream().filter(s -> "HIGH".equals(s.getPriority())).count();
        long mediumCount = allSuggestions.stream().filter(s -> "MEDIUM".equals(s.getPriority())).count();
        long lowCount = allSuggestions.stream().filter(s -> "LOW".equals(s.getPriority())).count();
        long newCount = allSuggestions.stream().filter(s -> "NEW".equals(s.getStatus())).count();
        long reviewedCount = allSuggestions.stream().filter(s -> "REVIEWED".equals(s.getStatus())).count();
        long inProgressCount = allSuggestions.stream().filter(s -> "IN PROGRESS".equals(s.getStatus())).count();
        long implementedCount = allSuggestions.stream().filter(s -> "IMPLEMENTED".equals(s.getStatus())).count();

        Map<String, Long> categoryCount = allSuggestions.stream()
                .collect(Collectors.groupingBy(Suggestion::getCategory, Collectors.counting()));

        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        List<SentimentResult> allResults = resultRepository.findAll();
        List<String> negativeKeywords = suggestionService.getNegativeKeywordsForRecord(allFeedbacks, allResults);

        model.addAttribute("suggestions", allSuggestions);
        model.addAttribute("highCount", highCount);
        model.addAttribute("mediumCount", mediumCount);
        model.addAttribute("lowCount", lowCount);
        model.addAttribute("newCount", newCount);
        model.addAttribute("reviewedCount", reviewedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("implementedCount", implementedCount);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("negativeKeywords", negativeKeywords);
        model.addAttribute("allAnalysisRecords", dashboardService.getAllAnalysisRecords());

        return "admin/suggestions";
    }

    @PostMapping("/suggestions/status/{id}")
    public String updateSuggestionStatus(@PathVariable Long id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        try {
            suggestionService.updateSuggestionStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Suggestion status updated to " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating suggestion: " + e.getMessage());
        }
        return "redirect:/admin/suggestions";
    }

    @PostMapping("/suggestions/action-plan/update")
    public String adminUpdateActionPlan(
            @RequestParam("suggestionId") Long suggestionId,
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
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating action plan: " + e.getMessage());
        }
        return "redirect:/admin/suggestions";
    }

    @PostMapping("/suggestions/delete/{id}")
    public String deleteSuggestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            suggestionService.deleteSuggestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Suggestion deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting suggestion: " + e.getMessage());
        }
        return "redirect:/admin/suggestions";
    }

    @PostMapping("/suggestions/add")
    public String addCustomSuggestion(
            @RequestParam("recordId") Long recordId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "suggestionText", required = false) String suggestionText,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "actionPlan", required = false) String actionPlan,
            @RequestParam(value = "responsiblePerson", required = false) String responsiblePerson,
            @RequestParam(value = "targetDate", required = false) String targetDate,
            @RequestParam(value = "remarks", required = false) String remarks,
            RedirectAttributes redirectAttributes) {
        try {
            suggestionService.addCustomSuggestion(recordId, category, suggestionText, priority, status, actionPlan, responsiblePerson, targetDate, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Custom recommendation added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding recommendation: " + e.getMessage());
        }
        return "redirect:/admin/suggestions";
    }

    // ===== DB CHART PAGE =====

    @GetMapping("/dbchart")
    public String dbChart(Model model) {
        // --- Table row counts ---
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("uploadsCount", uploadRepository.count());
        model.addAttribute("feedbackCount", feedbackRepository.count());
        model.addAttribute("analysisCount", dashboardService.getAllAnalysisRecords().size());
        model.addAttribute("sentimentResultsCount", resultRepository.count());
        model.addAttribute("dictionaryCount", sentimentDictionaryRepository.count());
        model.addAttribute("suggestionsCount", suggestionRepository.count());
        model.addAttribute("activitiesCount", userActivityRepository.count());

        // --- Users breakdown ---
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long clientCount = userRepository.countByRole(Role.CLIENT);
        long activeCount = userRepository.countByStatus("Active");
        long inactiveCount = userRepository.count() - activeCount;
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("clientCount", clientCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);

        // --- Sentiment dictionary breakdown ---
        long posWords = sentimentDictionaryRepository.countBySentimentType(SentimentType.POSITIVE);
        long negWords = sentimentDictionaryRepository.countBySentimentType(SentimentType.NEGATIVE);
        model.addAttribute("posWords", posWords);
        model.addAttribute("negWords", negWords);

        // --- Sentiment results breakdown ---
        long posFeedback = dashboardService.getTotalPositive();
        long negFeedback = dashboardService.getTotalNegative();
        long neuFeedback = dashboardService.getTotalNeutral();
        model.addAttribute("posFeedback", posFeedback);
        model.addAttribute("negFeedback", negFeedback);
        model.addAttribute("neuFeedback", neuFeedback);

        // --- Suggestions breakdown ---
        long highSuggestions = suggestionRepository.countByPriority("HIGH");
        long medSuggestions = suggestionRepository.countByPriority("MEDIUM");
        long lowSuggestions = suggestionRepository.countByPriority("LOW");
        long newSugg = suggestionRepository.countByStatus("NEW");
        long reviewedSugg = suggestionRepository.countByStatus("REVIEWED");
        long inProgressSugg = suggestionRepository.countByStatus("IN PROGRESS");
        long implementedSugg = suggestionRepository.countByStatus("IMPLEMENTED");
        model.addAttribute("highSuggestions", highSuggestions);
        model.addAttribute("medSuggestions", medSuggestions);
        model.addAttribute("lowSuggestions", lowSuggestions);
        model.addAttribute("newSugg", newSugg);
        model.addAttribute("reviewedSugg", reviewedSugg);
        model.addAttribute("inProgressSugg", inProgressSugg);
        model.addAttribute("implementedSugg", implementedSugg);

        // --- Activity breakdown ---
        long loginCount = userActivityRepository.countByActivityType(ActivityType.LOGIN);
        long logoutCount = userActivityRepository.countByActivityType(ActivityType.LOGOUT);
        long uploadActivity = userActivityRepository.countByActivityType(ActivityType.FEEDBACK_UPLOAD);
        long analysisActivity = userActivityRepository.countByActivityType(ActivityType.SENTIMENT_ANALYSIS);
        long reportActivity = userActivityRepository.countByActivityType(ActivityType.REPORT_GENERATED);
        long registrationActivity = userActivityRepository.countByActivityType(ActivityType.REGISTRATION);
        model.addAttribute("loginCount", loginCount);
        model.addAttribute("logoutCount", logoutCount);
        model.addAttribute("uploadActivity", uploadActivity);
        model.addAttribute("analysisActivity", analysisActivity);
        model.addAttribute("reportActivity", reportActivity);
        model.addAttribute("registrationActivity", registrationActivity);

        // --- Upload status breakdown ---
        List<FeedbackUpload> allUploads = uploadRepository.findAll();
        long analyzedUploads = allUploads.stream().filter(u -> "Analyzed".equals(u.getStatus())).count();
        long pendingUploads = allUploads.size() - analyzedUploads;
        model.addAttribute("analyzedUploads", analyzedUploads);
        model.addAttribute("pendingUploads", pendingUploads);

        // --- Per-faculty upload counts for bar chart ---
        List<User> facultyList = userRepository.findByRole(Role.CLIENT);
        Map<String, Long> facultyUploadMap = new HashMap<>();
        for (User f : facultyList) {
            facultyUploadMap.put(f.getName(), (long) uploadRepository.findByUploadedByOrderByUploadDateDesc(f).size());
        }
        model.addAttribute("facultyUploadMap", facultyUploadMap);

        // --- Analysis records for timeline ---
        model.addAttribute("allAnalysisRecords", dashboardService.getAllAnalysisRecords());

        return "admin/dbchart";
    }
}
