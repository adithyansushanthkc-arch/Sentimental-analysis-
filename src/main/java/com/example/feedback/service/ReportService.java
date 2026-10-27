package com.example.feedback.service;

import com.example.feedback.entity.AnalysisRecord;
import com.example.feedback.entity.SentimentResult;
import com.example.feedback.entity.Suggestion;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    private final SuggestionService suggestionService;

    public ReportService(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    public byte[] generatePdfReport(AnalysisRecord record, List<SentimentResult> results) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Student Feedback Sentiment Analysis Report", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph(" "));
            
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font fontBold14 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontBold12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            document.add(new Paragraph("File Name: " + record.getUpload().getFileName(), fontNormal));
            document.add(new Paragraph("Analysis Date: " + record.getAnalysisDate(), fontNormal));
            document.add(new Paragraph("Uploaded By: " + record.getUpload().getUploadedBy().getName(), fontNormal));
            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Summary Statistics", fontBold14));
            document.add(new Paragraph("Total Feedback: " + record.getTotalFeedback(), fontNormal));
            document.add(new Paragraph("Positive Feedback: " + record.getPositiveCount(), fontNormal));
            document.add(new Paragraph("Negative Feedback: " + record.getNegativeCount(), fontNormal));
            document.add(new Paragraph("Neutral Feedback: " + record.getNeutralCount(), fontNormal));
            
            double posPerc = record.getTotalFeedback() > 0 ? (double) record.getPositiveCount() / record.getTotalFeedback() * 100 : 0;
            double negPerc = record.getTotalFeedback() > 0 ? (double) record.getNegativeCount() / record.getTotalFeedback() * 100 : 0;
            double neuPerc = record.getTotalFeedback() > 0 ? (double) record.getNeutralCount() / record.getTotalFeedback() * 100 : 0;
            
            document.add(new Paragraph(String.format("Positive Percentage: %.2f%%", posPerc), fontNormal));
            document.add(new Paragraph(String.format("Negative Percentage: %.2f%%", negPerc), fontNormal));
            document.add(new Paragraph(String.format("Neutral Percentage: %.2f%%", neuPerc), fontNormal));
            document.add(new Paragraph(String.format("Average Sentiment Score: %.2f", record.getAverageScore()), fontNormal));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Insights", fontBold14));
            if (posPerc > 50) {
                document.add(new Paragraph("- Most students have provided positive feedback.", fontNormal));
            } else if (negPerc > 50) {
                document.add(new Paragraph("- A significant number of students have provided negative feedback. Review required.", fontNormal));
            } else {
                document.add(new Paragraph("- The feedback is generally mixed/neutral.", fontNormal));
            }

            // Future Suggestions & Recommendations Section
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Future Suggestions & Recommendations", fontBold14));
            document.add(new Paragraph("Based on the sentiment analysis, the following improvements are recommended:", fontNormal));
            document.add(new Paragraph(" "));

            List<Suggestion> suggestions = suggestionService.getSuggestionsForRecord(record);
            if (suggestions.isEmpty()) {
                document.add(new Paragraph("- No specific critical improvement areas were identified.", fontNormal));
            } else {
                int idx = 1;
                for (Suggestion s : suggestions) {
                    document.add(new Paragraph(String.format("%d. %s (Priority: %s)", idx++, s.getCategory(), s.getPriority()), fontBold12));
                    document.add(new Paragraph("   Suggestion: " + s.getSuggestionText(), fontNormal));
                    document.add(new Paragraph("   Recommended Action: " + s.getActionPlan(), fontNormal));
                    document.add(new Paragraph(String.format("   Related Feedback Responses: %d", s.getRelatedFeedbackCount()), fontNormal));
                    document.add(new Paragraph(" "));
                }
            }
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
