-- ============================================================
-- Student Feedback Sentiment Analysis System
-- MySQL Database Setup Script
-- ============================================================
-- Run this script in MySQL as root (or a user with CREATE privileges):
--   mysql -u root -p < database/setup.sql
-- ============================================================

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS student_feedback_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE student_feedback_db;

-- ============================================================
-- 2. Create Tables
-- ============================================================

-- 2a. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,             -- ADMIN, CLIENT
    status VARCHAR(20) NOT NULL,           -- Active, Inactive
    last_login DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2b. Sentiment Dictionary
CREATE TABLE IF NOT EXISTS sentiment_dictionary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(255) NOT NULL UNIQUE,
    sentiment_type VARCHAR(20) NOT NULL,   -- POSITIVE, NEGATIVE, NEUTRAL
    weight INT NOT NULL,
    status VARCHAR(20) NOT NULL,           -- Active, Inactive
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2c. Feedback Upload
CREATE TABLE IF NOT EXISTS feedback_upload (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    total_records INT NULL,
    upload_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,           -- Uploaded, Analyzed, Error
    CONSTRAINT fk_upload_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2d. Feedback
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upload_id BIGINT NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    faculty VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    feedback_text TEXT NOT NULL,
    CONSTRAINT fk_feedback_upload FOREIGN KEY (upload_id) REFERENCES feedback_upload(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2e. Analysis Record
CREATE TABLE IF NOT EXISTS analysis_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upload_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_feedback INT NULL,
    positive_count INT NULL,
    negative_count INT NULL,
    neutral_count INT NULL,
    average_score DOUBLE NULL,
    analysis_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_upload FOREIGN KEY (upload_id) REFERENCES feedback_upload(id) ON DELETE CASCADE,
    CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2f. Sentiment Result
CREATE TABLE IF NOT EXISTS sentiment_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL UNIQUE,
    sentiment VARCHAR(20) NOT NULL,        -- POSITIVE, NEGATIVE, NEUTRAL
    sentiment_score INT NOT NULL,
    positive_word_count INT NOT NULL,
    negative_word_count INT NOT NULL,
    analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_feedback FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2g. Suggestions
CREATE TABLE IF NOT EXISTS suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    category VARCHAR(255) NOT NULL,
    suggestion_text TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,         -- HIGH, MEDIUM, LOW
    related_feedback_count INT NULL,
    status VARCHAR(30) NOT NULL,           -- NEW, REVIEWED, IN_PROGRESS, IMPLEMENTED
    action_plan TEXT NULL,
    responsible_person VARCHAR(255) NULL,
    target_date VARCHAR(255) NULL,
    remarks TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_suggestion_analysis FOREIGN KEY (analysis_id) REFERENCES analysis_record(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2h. User Activities
CREATE TABLE IF NOT EXISTS user_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    user_email VARCHAR(255) NULL,
    user_name VARCHAR(255) NULL,
    user_role VARCHAR(20) NULL,
    activity_type VARCHAR(30) NOT NULL,    -- REGISTRATION, LOGIN, LOGOUT, FEEDBACK_UPLOAD, SENTIMENT_ANALYSIS, REPORT_GENERATED
    details VARCHAR(500) NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;


-- ============================================================
-- 3. Insert Sample Data
-- ============================================================
-- NOTE: All passwords are BCrypt-encoded.
--   admin123    -> $2a$10$4Tf3HVQlu3okz6NjSSexkuTMTZdBKaiumzJrKu8UEONBCpgdavd7e
--   client123   -> $2a$10$RZxS/IWR/nj8GgM7gXJr5upXCfOI4zFxMsOIV6wN1DECHJntfAnli
--   faculty123  -> $2a$10$ULmlMpL5lAE5/V4P./04mu1Il7RYphqoL6XfohpO2hYN2SxSjSKQi

-- 3a. Users (1 Admin + 2 Faculty/Client)
INSERT INTO users (id, name, email, password, role, status, last_login, created_at, updated_at) VALUES
(1, 'System Admin',   'admin@example.com',   '$2a$10$4Tf3HVQlu3okz6NjSSexkuTMTZdBKaiumzJrKu8UEONBCpgdavd7e', 'ADMIN',  'Active', NOW(), NOW(), NOW()),
(2, 'Faculty Member', 'client@example.com',  '$2a$10$RZxS/IWR/nj8GgM7gXJr5upXCfOI4zFxMsOIV6wN1DECHJntfAnli', 'CLIENT', 'Active', NOW(), NOW(), NOW()),
(3, 'Dr. Smith',      'faculty@example.com', '$2a$10$ULmlMpL5lAE5/V4P./04mu1Il7RYphqoL6XfohpO2hYN2SxSjSKQi', 'CLIENT', 'Active', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE password = VALUES(password);


-- 3b. Sentiment Dictionary (20 Positive + 20 Negative words)
INSERT INTO sentiment_dictionary (word, sentiment_type, weight, status, created_at) VALUES
-- Positive words
('excellent',      'POSITIVE',  2, 'Active', NOW()),
('good',           'POSITIVE',  1, 'Active', NOW()),
('great',          'POSITIVE',  2, 'Active', NOW()),
('amazing',        'POSITIVE',  2, 'Active', NOW()),
('helpful',        'POSITIVE',  1, 'Active', NOW()),
('clear',          'POSITIVE',  1, 'Active', NOW()),
('effective',      'POSITIVE',  1, 'Active', NOW()),
('friendly',       'POSITIVE',  1, 'Active', NOW()),
('best',           'POSITIVE',  2, 'Active', NOW()),
('wonderful',      'POSITIVE',  2, 'Active', NOW()),
('outstanding',    'POSITIVE',  2, 'Active', NOW()),
('fantastic',      'POSITIVE',  2, 'Active', NOW()),
('engaging',       'POSITIVE',  1, 'Active', NOW()),
('informative',    'POSITIVE',  1, 'Active', NOW()),
('supportive',     'POSITIVE',  1, 'Active', NOW()),
('interactive',    'POSITIVE',  1, 'Active', NOW()),
('motivating',     'POSITIVE',  1, 'Active', NOW()),
('knowledgeable',  'POSITIVE',  1, 'Active', NOW()),
('inspiring',      'POSITIVE',  2, 'Active', NOW()),
('organized',      'POSITIVE',  1, 'Active', NOW()),
-- Negative words
('bad',            'NEGATIVE', -1, 'Active', NOW()),
('poor',           'NEGATIVE', -1, 'Active', NOW()),
('worst',          'NEGATIVE', -2, 'Active', NOW()),
('difficult',      'NEGATIVE', -1, 'Active', NOW()),
('unclear',        'NEGATIVE', -1, 'Active', NOW()),
('boring',         'NEGATIVE', -1, 'Active', NOW()),
('useless',        'NEGATIVE', -2, 'Active', NOW()),
('unsatisfied',    'NEGATIVE', -1, 'Active', NOW()),
('slow',           'NEGATIVE', -1, 'Active', NOW()),
('confusing',      'NEGATIVE', -1, 'Active', NOW()),
('terrible',       'NEGATIVE', -2, 'Active', NOW()),
('unorganized',    'NEGATIVE', -1, 'Active', NOW()),
('rushed',         'NEGATIVE', -1, 'Active', NOW()),
('unhelpful',      'NEGATIVE', -1, 'Active', NOW()),
('monotonous',     'NEGATIVE', -1, 'Active', NOW()),
('disappointing',  'NEGATIVE', -1, 'Active', NOW()),
('frustrating',    'NEGATIVE', -1, 'Active', NOW()),
('irrelevant',     'NEGATIVE', -1, 'Active', NOW()),
('unprepared',     'NEGATIVE', -1, 'Active', NOW()),
('disorganized',   'NEGATIVE', -1, 'Active', NOW())
ON DUPLICATE KEY UPDATE word = VALUES(word);


-- 3c. Feedback Uploads (2 CSV uploads by faculty users)
INSERT INTO feedback_upload (id, file_name, uploaded_by, total_records, upload_date, status) VALUES
(1, 'java_feedback_sem1.csv',      2, 10, '2026-08-15 10:30:00', 'Analyzed'),
(2, 'python_feedback_sem1.csv',    3,  8, '2026-08-20 14:15:00', 'Analyzed');


-- 3d. Feedback records — Upload 1 (Java course, 10 students)
INSERT INTO feedback (id, upload_id, student_id, faculty, subject, feedback_text) VALUES
(1,  1, 'S001', 'Faculty Member', 'Java Programming', 'The teacher explains concepts very clearly and the examples are excellent'),
(2,  1, 'S002', 'Faculty Member', 'Java Programming', 'Classes are boring and difficult to follow, the pace is too slow'),
(3,  1, 'S003', 'Faculty Member', 'Java Programming', 'Great teaching style, very engaging and interactive sessions'),
(4,  1, 'S004', 'Faculty Member', 'Java Programming', 'The subject material is confusing and the explanations are unclear'),
(5,  1, 'S005', 'Faculty Member', 'Java Programming', 'Good overall experience, the faculty is knowledgeable and supportive'),
(6,  1, 'S006', 'Faculty Member', 'Java Programming', 'Amazing lectures with real world examples, best faculty I have had'),
(7,  1, 'S007', 'Faculty Member', 'Java Programming', 'The lab sessions feel useless and the assignments are irrelevant'),
(8,  1, 'S008', 'Faculty Member', 'Java Programming', 'Helpful and friendly approach, makes even complex topics clear'),
(9,  1, 'S009', 'Faculty Member', 'Java Programming', 'Content is okay but nothing special about the teaching method'),
(10, 1, 'S010', 'Faculty Member', 'Java Programming', 'Outstanding performance by the teacher, wonderful learning experience');

-- Feedback records — Upload 2 (Python course, 8 students)
INSERT INTO feedback (id, upload_id, student_id, faculty, subject, feedback_text) VALUES
(11, 2, 'S011', 'Dr. Smith', 'Python Programming', 'Excellent course structure, the faculty is very organized and informative'),
(12, 2, 'S012', 'Dr. Smith', 'Python Programming', 'The classes are rushed and the teacher seems unprepared sometimes'),
(13, 2, 'S013', 'Dr. Smith', 'Python Programming', 'Very effective teaching with clear explanations and good examples'),
(14, 2, 'S014', 'Dr. Smith', 'Python Programming', 'Terrible experience, the course content is disorganized and monotonous'),
(15, 2, 'S015', 'Dr. Smith', 'Python Programming', 'The faculty is motivating and inspiring, fantastic learning environment'),
(16, 2, 'S016', 'Dr. Smith', 'Python Programming', 'Poor lab facilities and unhelpful teaching assistants made it frustrating'),
(17, 2, 'S017', 'Dr. Smith', 'Python Programming', 'Decent course, nothing too good or too bad about the teaching'),
(18, 2, 'S018', 'Dr. Smith', 'Python Programming', 'Great interactive sessions with wonderful practical assignments');


-- 3e. Sentiment Results — Upload 1
INSERT INTO sentiment_result (id, feedback_id, sentiment, sentiment_score, positive_word_count, negative_word_count, analyzed_at) VALUES
(1,  1,  'POSITIVE',  3, 2, 0, '2026-08-15 10:35:00'),
(2,  2,  'NEGATIVE', -3, 0, 3, '2026-08-15 10:35:00'),
(3,  3,  'POSITIVE',  3, 2, 0, '2026-08-15 10:35:00'),
(4,  4,  'NEGATIVE', -2, 0, 2, '2026-08-15 10:35:00'),
(5,  5,  'POSITIVE',  3, 3, 0, '2026-08-15 10:35:00'),
(6,  6,  'POSITIVE',  6, 3, 0, '2026-08-15 10:35:00'),
(7,  7,  'NEGATIVE', -4, 0, 2, '2026-08-15 10:35:00'),
(8,  8,  'POSITIVE',  3, 3, 0, '2026-08-15 10:35:00'),
(9,  9,  'NEUTRAL',   0, 0, 0, '2026-08-15 10:35:00'),
(10, 10, 'POSITIVE',  4, 2, 0, '2026-08-15 10:35:00');

-- Sentiment Results — Upload 2
INSERT INTO sentiment_result (id, feedback_id, sentiment, sentiment_score, positive_word_count, negative_word_count, analyzed_at) VALUES
(11, 11, 'POSITIVE',  4, 3, 0, '2026-08-20 14:20:00'),
(12, 12, 'NEGATIVE', -2, 0, 2, '2026-08-20 14:20:00'),
(13, 13, 'POSITIVE',  3, 3, 0, '2026-08-20 14:20:00'),
(14, 14, 'NEGATIVE', -4, 0, 3, '2026-08-20 14:20:00'),
(15, 15, 'POSITIVE',  5, 3, 0, '2026-08-20 14:20:00'),
(16, 16, 'NEGATIVE', -3, 0, 3, '2026-08-20 14:20:00'),
(17, 17, 'NEUTRAL',   0, 0, 0, '2026-08-20 14:20:00'),
(18, 18, 'POSITIVE',  4, 2, 0, '2026-08-20 14:20:00');


-- 3f. Analysis Records (summary for each upload)
INSERT INTO analysis_record (id, upload_id, user_id, total_feedback, positive_count, negative_count, neutral_count, average_score, analysis_date) VALUES
(1, 1, 2, 10, 6, 3, 1,  1.30, '2026-08-15 10:35:00'),
(2, 2, 3,  8, 4, 3, 1,  0.88, '2026-08-20 14:20:00');


-- 3g. Suggestions (auto-generated from analysis)
INSERT INTO suggestions (id, analysis_id, category, suggestion_text, priority, related_feedback_count, status, action_plan, responsible_person, target_date, remarks, created_at, updated_at) VALUES
(1, 1, 'Teaching Methodology',   'Multiple students found lectures boring and difficult. Consider incorporating more interactive teaching methods such as group discussions, live coding sessions, and quizzes.',                       'HIGH',   3, 'NEW',         NULL, NULL, NULL, NULL, '2026-08-15 10:36:00', '2026-08-15 10:36:00'),
(2, 1, 'Lab Sessions',           'Lab sessions were described as useless with irrelevant assignments. Review and align lab exercises with current course objectives and real-world applications.',                                   'HIGH',   1, 'IN_PROGRESS', 'Redesign lab assignments to include practical mini-projects', 'Faculty Member', '2026-10-01', 'Lab coordinator notified', '2026-08-15 10:36:00', '2026-08-20 09:00:00'),
(3, 1, 'Content Clarity',        'Some students found explanations unclear and confusing. Provide supplementary materials like lecture notes, recorded videos, and additional reference links.',                                    'MEDIUM', 2, 'REVIEWED',    NULL, NULL, NULL, NULL, '2026-08-15 10:36:00', '2026-08-16 11:00:00'),
(4, 1, 'Positive Reinforcement', 'Strong positive feedback on real-world examples and clear explanations. Continue and expand this approach across all modules.',                                                                  'LOW',    6, 'IMPLEMENTED', 'Maintain current teaching approach and share best practices with department', 'Faculty Member', '2026-09-01', 'Documented as department best practice', '2026-08-15 10:36:00', '2026-08-25 10:00:00'),
(5, 2, 'Course Pacing',          'Feedback indicates classes feel rushed and the instructor appears unprepared at times. Allocate more preparation time and consider slowing the pace during complex topics.',                     'HIGH',   2, 'NEW',         NULL, NULL, NULL, NULL, '2026-08-20 14:21:00', '2026-08-20 14:21:00'),
(6, 2, 'Lab Infrastructure',     'Students reported poor lab facilities and unhelpful teaching assistants. Upgrade lab equipment and provide TA training workshops.',                                                               'HIGH',   1, 'NEW',         NULL, NULL, NULL, NULL, '2026-08-20 14:21:00', '2026-08-20 14:21:00'),
(7, 2, 'Course Organization',    'Course content was described as disorganized and monotonous. Restructure the syllabus with clear weekly milestones, varied content delivery methods, and regular checkpoints.',                  'MEDIUM', 1, 'REVIEWED',    NULL, NULL, NULL, NULL, '2026-08-20 14:21:00', '2026-08-22 09:00:00'),
(8, 2, 'Positive Reinforcement', 'Students praised interactive sessions, practical assignments, and an inspiring teaching environment. Continue leveraging these strengths.',                                                       'LOW',    4, 'IMPLEMENTED', 'Interactive teaching methods documented and shared', 'Dr. Smith', '2026-09-15', 'Added to teaching portfolio', '2026-08-20 14:21:00', '2026-08-28 10:00:00');


-- 3h. User Activities (login/activity history)
INSERT INTO user_activities (user_id, user_email, user_name, user_role, activity_type, details, timestamp) VALUES
(1, 'admin@example.com',   'System Admin',   'ADMIN',  'REGISTRATION',      'New user created/updated: admin@example.com',   '2026-08-01 09:00:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'REGISTRATION',      'New user created/updated: client@example.com',  '2026-08-01 09:01:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'REGISTRATION',      'New user created/updated: faculty@example.com', '2026-08-01 09:02:00'),
(1, 'admin@example.com',   'System Admin',   'ADMIN',  'LOGIN',             'Admin logged in',                               '2026-08-10 08:30:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'LOGIN',             'Faculty logged in',                             '2026-08-15 10:25:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'FEEDBACK_UPLOAD',   'Uploaded java_feedback_sem1.csv (10 records)',   '2026-08-15 10:30:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'SENTIMENT_ANALYSIS','Analysis completed for upload #1',              '2026-08-15 10:35:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'REPORT_GENERATED',  'PDF report generated for upload #1',            '2026-08-15 10:40:00'),
(2, 'client@example.com',  'Faculty Member', 'CLIENT', 'LOGOUT',            'Faculty logged out',                            '2026-08-15 11:00:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'LOGIN',             'Faculty logged in',                             '2026-08-20 14:10:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'FEEDBACK_UPLOAD',   'Uploaded python_feedback_sem1.csv (8 records)',  '2026-08-20 14:15:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'SENTIMENT_ANALYSIS','Analysis completed for upload #2',              '2026-08-20 14:20:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'REPORT_GENERATED',  'PDF report generated for upload #2',            '2026-08-20 14:25:00'),
(3, 'faculty@example.com', 'Dr. Smith',      'CLIENT', 'LOGOUT',            'Faculty logged out',                            '2026-08-20 15:00:00'),
(1, 'admin@example.com',   'System Admin',   'ADMIN',  'LOGIN',             'Admin logged in',                               '2026-08-22 09:00:00'),
(1, 'admin@example.com',   'System Admin',   'ADMIN',  'LOGOUT',            'Admin logged out',                              '2026-08-22 10:00:00');


-- ============================================================
-- Done! The database is ready.
-- ============================================================
-- Login credentials:
--   Admin:   admin@example.com   / admin123
--   Faculty: client@example.com  / client123
--   Faculty: faculty@example.com / faculty123
-- ============================================================
