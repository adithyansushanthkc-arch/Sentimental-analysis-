# Student Feedback Sentiment Analysis System

A comprehensive Java Spring Boot web application designed for analyzing and visualizing student feedback using a dictionary-based Natural Language Processing (NLP) algorithm. 

## Features
- **Role-based Access Control:** Secure Admin and Faculty (Client) modules.
- **CSV Data Ingestion:** Upload bulk student feedback in CSV format.
- **NLP Sentiment Analysis:** Text normalization, tokenization, and matching against a dynamic sentiment dictionary to classify feedback (Positive, Negative, Neutral).
- **Dashboard & Analytics:** Interactive Chart.js graphs and detailed statistics.
- **PDF Reports:** Automatic generation of professional PDF analysis reports using OpenPDF.
- **Dynamic Dictionary:** Admin module to configure positive/negative words and their weights.

## Technology Stack
- **Backend:** Java 17, Spring Boot, Spring Data JPA, Spring Security
- **Frontend:** HTML5, Bootstrap 5, Thymeleaf, Chart.js, FontAwesome
- **Database:** MySQL
- **Build Tool:** Maven
- **Utilities:** OpenCSV (parsing), OpenPDF (reports), BCrypt (password hashing)

## Architecture
The application follows a clean MVC (Model-View-Controller) architecture:
1. **Controller Layer:** Handles HTTP requests and template routing (`AdminController`, `ClientController`).
2. **Service Layer:** Business logic including CSV parsing, Sentiment calculation, and PDF generation.
3. **Repository Layer:** Spring Data JPA interfaces communicating with MySQL.
4. **View Layer:** Thymeleaf templates providing a responsive Bootstrap UI.

## Database Setup
1. Install MySQL Server.
2. Create the database:
   ```sql
   CREATE DATABASE student_feedback_db;
   ```
3. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

*(Note: Tables are automatically generated via Hibernate `update` ddl-auto).*

## How to Run
1. Ensure Java 17 and Maven are installed.
2. Navigate to the project root directory.
3. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
4. Alternatively, package it into a JAR and run it:
   ```bash
   mvn clean package
   java -jar target/student-feedback-sentiment-analysis-0.0.1-SNAPSHOT.jar
   ```
5. Open your browser and navigate to: `http://localhost:8080`

## Login Information
The database seeds default users automatically on first run:
- **Admin Login:** 
  - Email: `admin@example.com`
  - Password: `admin123`
- **Faculty Login:** 
  - Email: `faculty@example.com`
  - Password: `faculty123`

## CSV Format
When uploading feedback as a Faculty member, ensure the CSV file has headers and follows this exact column order:
`Student_ID, Faculty, Subject, Feedback`

**Example:**
```csv
Student_ID,Faculty,Subject,Feedback
S001,John Smith,Java,"The teacher explains concepts very clearly"
S002,John Smith,Java,"The classes are boring and difficult"
```

## Sentiment Analysis Algorithm
The system uses a dictionary-based approach:
1. **Normalization:** Input text is converted to lowercase and punctuation is stripped.
2. **Tokenization:** Text is split into words based on whitespace.
3. **Scoring:** Words are cross-referenced with the Admin-managed `SentimentDictionary`.
   - Positive words add to the score.
   - Negative words subtract from the score.
4. **Classification:** 
   - Score > 0: POSITIVE
   - Score < 0: NEGATIVE
   - Score == 0: NEUTRAL

## Future Scope
- Integration with external ML APIs (e.g., Gemini, OpenAI) for advanced contextual sentiment detection.
- Department-level grouping and multi-tenancy.
- Exporting history records to Excel.

## Troubleshooting
- **Database Connection Error:** Ensure MySQL is running on port `3306` and credentials in `application.properties` are correct.
- **Port 8080 in use:** Change `server.port=8081` in `application.properties`.
- **CSV Upload Error:** Ensure the CSV is not empty and the columns match exactly as specified.
