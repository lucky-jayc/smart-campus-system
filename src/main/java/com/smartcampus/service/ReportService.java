package com.smartcampus.service;

import com.smartcampus.model.*;
import com.smartcampus.repository.AcademicYearRepository;
import com.smartcampus.repository.CourseRegistrationRepository;
import com.smartcampus.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final GradingService gradingService;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;

    public ReportService(GradingService gradingService,
                         CourseRegistrationRepository courseRegistrationRepository,
                         AcademicYearRepository academicYearRepository,
                         SemesterRepository semesterRepository) {
        this.gradingService = gradingService;
        this.courseRegistrationRepository = courseRegistrationRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
    }

    // --- Transcript Data Structure ---
    public static class SemesterTranscriptItem {
        private final Semester semester;
        private final AcademicYear academicYear;
        private final List<Grade> grades;
        private final double gpa;
        private final int totalCreditUnits;

        public SemesterTranscriptItem(Semester semester, AcademicYear academicYear, List<Grade> grades, double gpa, int totalCreditUnits) {
            this.semester = semester;
            this.academicYear = academicYear;
            this.grades = grades;
            this.gpa = gpa;
            this.totalCreditUnits = totalCreditUnits;
        }

        public Semester getSemester() { return semester; }
        public AcademicYear getAcademicYear() { return academicYear; }
        public List<Grade> getGrades() { return grades; }
        public double getGpa() { return gpa; }
        public int getTotalCreditUnits() { return totalCreditUnits; }
    }

    public static class StudentTranscript {
        private final Student student;
        private final List<SemesterTranscriptItem> semesterRecords;
        private final double cumulativeGPA;
        private final int totalCreditsEarned;
        private final String academicStanding;

        public StudentTranscript(Student student, List<SemesterTranscriptItem> semesterRecords, double cumulativeGPA, int totalCreditsEarned, String academicStanding) {
            this.student = student;
            this.semesterRecords = semesterRecords;
            this.cumulativeGPA = cumulativeGPA;
            this.totalCreditsEarned = totalCreditsEarned;
            this.academicStanding = academicStanding;
        }

        public Student getStudent() { return student; }
        public List<SemesterTranscriptItem> getSemesterRecords() { return semesterRecords; }
        public double getCumulativeGPA() { return cumulativeGPA; }
        public int getTotalCreditsEarned() { return totalCreditsEarned; }
        public String getAcademicStanding() { return academicStanding; }
    }

    public static class CoursePerformanceReport {
        private final Course course;
        private final AcademicYear academicYear;
        private final Semester semester;
        private final int totalStudents;
        private final double averageScore;
        private final int passCount;
        private final int failCount;
        private final double passRate;
        private final List<Grade> grades;

        public CoursePerformanceReport(Course course, AcademicYear academicYear, Semester semester, int totalStudents, double averageScore, int passCount, int failCount, double passRate, List<Grade> grades) {
            this.course = course;
            this.academicYear = academicYear;
            this.semester = semester;
            this.totalStudents = totalStudents;
            this.averageScore = averageScore;
            this.passCount = passCount;
            this.failCount = failCount;
            this.passRate = passRate;
            this.grades = grades;
        }

        public Course getCourse() { return course; }
        public AcademicYear getAcademicYear() { return academicYear; }
        public Semester getSemester() { return semester; }
        public int getTotalStudents() { return totalStudents; }
        public double getAverageScore() { return averageScore; }
        public int getPassCount() { return passCount; }
        public int getFailCount() { return failCount; }
        public double getPassRate() { return passRate; }
        public List<Grade> getGrades() { return grades; }
    }

    public StudentTranscript generateStudentTranscript(Student student) {
        List<CourseRegistration> allRegistrations = courseRegistrationRepository.findByStudent(student);

        // Group registrations by AcademicYear and Semester
        Map<String, List<CourseRegistration>> grouped = new LinkedHashMap<>();
        for (CourseRegistration reg : allRegistrations) {
            String key = reg.getAcademicYear().getName() + " - " + reg.getSemester().getName();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(reg);
        }

        List<SemesterTranscriptItem> semesterRecords = new ArrayList<>();
        int totalCreditsEarned = 0;

        for (Map.Entry<String, List<CourseRegistration>> entry : grouped.entrySet()) {
            List<CourseRegistration> regs = entry.getValue();
            if (regs.isEmpty()) continue;

            AcademicYear year = regs.get(0).getAcademicYear();
            Semester sem = regs.get(0).getSemester();

            List<Grade> termGrades = new ArrayList<>();
            int termCredits = 0;

            for (CourseRegistration reg : regs) {
                Optional<Grade> gradeOpt = gradingService.getGradeForRegistration(reg);
                if (gradeOpt.isPresent()) {
                    Grade g = gradeOpt.get();
                    termGrades.add(g);
                    if (g.getTotalMark() != null && g.getTotalMark() >= 50.0) {
                        termCredits += reg.getCourse().getCreditUnits();
                        totalCreditsEarned += reg.getCourse().getCreditUnits();
                    }
                }
            }

            double semGPA = gradingService.calculateSemesterGPA(student, year, sem);
            semesterRecords.add(new SemesterTranscriptItem(sem, year, termGrades, semGPA, termCredits));
        }

        double cgpa = gradingService.calculateCumulativeGPA(student);
        String standing = gradingService.getAcademicStanding(cgpa);

        return new StudentTranscript(student, semesterRecords, cgpa, totalCreditsEarned, standing);
    }

    public CoursePerformanceReport generateCourseReport(Course course, AcademicYear academicYear, Semester semester) {
        List<Grade> grades = gradingService.getGradesForCourseInTerm(course, academicYear, semester);
        int total = grades.size();
        if (total == 0) {
            return new CoursePerformanceReport(course, academicYear, semester, 0, 0.0, 0, 0, 0.0, Collections.emptyList());
        }

        double sum = 0.0;
        int pass = 0;
        int fail = 0;
        int gradedCount = 0;

        for (Grade g : grades) {
            if (g.getTotalMark() != null) {
                sum += g.getTotalMark();
                gradedCount++;
                if (g.getTotalMark() >= 50.0) {
                    pass++;
                } else {
                    fail++;
                }
            }
        }

        double avg = gradedCount > 0 ? (Math.round((sum / gradedCount) * 10.0) / 10.0) : 0.0;
        double passRate = gradedCount > 0 ? (Math.round(((double) pass / gradedCount) * 1000.0) / 10.0) : 0.0;

        return new CoursePerformanceReport(course, academicYear, semester, total, avg, pass, fail, passRate, grades);
    }
}
