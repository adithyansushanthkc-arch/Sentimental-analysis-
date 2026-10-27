package com.example.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_upload")
public class FeedbackUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "total_records")
    private Integer totalRecords;

    @CreationTimestamp
    @Column(name = "upload_date", updatable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private String status; // Uploaded, Analyzed, Error

    public FeedbackUpload() {}

    public FeedbackUpload(Long id, String fileName, User uploadedBy, Integer totalRecords, LocalDateTime uploadDate, String status) {
        this.id = id;
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.totalRecords = totalRecords;
        this.uploadDate = uploadDate;
        this.status = status;
    }

    public static FeedbackUploadBuilder builder() {
        return new FeedbackUploadBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static class FeedbackUploadBuilder {
        private Long id;
        private String fileName;
        private User uploadedBy;
        private Integer totalRecords;
        private LocalDateTime uploadDate;
        private String status;

        public FeedbackUploadBuilder id(Long id) { this.id = id; return this; }
        public FeedbackUploadBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public FeedbackUploadBuilder uploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public FeedbackUploadBuilder totalRecords(Integer totalRecords) { this.totalRecords = totalRecords; return this; }
        public FeedbackUploadBuilder uploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; return this; }
        public FeedbackUploadBuilder status(String status) { this.status = status; return this; }

        public FeedbackUpload build() {
            return new FeedbackUpload(id, fileName, uploadedBy, totalRecords, uploadDate, status);
        }
    }
}
