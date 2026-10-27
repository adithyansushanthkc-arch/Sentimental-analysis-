package com.example.feedback.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    private FeedbackUpload upload;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String faculty;

    @Column(nullable = false)
    private String subject;

    @Column(name = "feedback_text", columnDefinition = "TEXT", nullable = false)
    private String feedbackText;

    public Feedback() {}

    public Feedback(Long id, FeedbackUpload upload, String studentId, String faculty, String subject, String feedbackText) {
        this.id = id;
        this.upload = upload;
        this.studentId = studentId;
        this.faculty = faculty;
        this.subject = subject;
        this.feedbackText = feedbackText;
    }

    public static FeedbackBuilder builder() {
        return new FeedbackBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeedbackUpload getUpload() { return upload; }
    public void setUpload(FeedbackUpload upload) { this.upload = upload; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public static class FeedbackBuilder {
        private Long id;
        private FeedbackUpload upload;
        private String studentId;
        private String faculty;
        private String subject;
        private String feedbackText;

        public FeedbackBuilder id(Long id) { this.id = id; return this; }
        public FeedbackBuilder upload(FeedbackUpload upload) { this.upload = upload; return this; }
        public FeedbackBuilder studentId(String studentId) { this.studentId = studentId; return this; }
        public FeedbackBuilder faculty(String faculty) { this.faculty = faculty; return this; }
        public FeedbackBuilder subject(String subject) { this.subject = subject; return this; }
        public FeedbackBuilder feedbackText(String feedbackText) { this.feedbackText = feedbackText; return this; }

        public Feedback build() {
            return new Feedback(id, upload, studentId, faculty, subject, feedbackText);
        }
    }
}
