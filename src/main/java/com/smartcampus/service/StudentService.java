package com.smartcampus.service;

import com.smartcampus.model.*;
import com.smartcampus.repository.CourseRegistrationRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final UserService userService;

    public StudentService(StudentRepository studentRepository,
                          CourseRegistrationRepository courseRegistrationRepository,
                          UserService userService) {
        this.studentRepository = studentRepository;
        this.courseRegistrationRepository = courseRegistrationRepository;
        this.userService = userService;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByRegistrationNumber(String regNumber) {
        return studentRepository.findByRegistrationNumber(regNumber);
    }

    public Optional<Student> getStudentByUsername(String username) {
        return studentRepository.findByUserUsername(username);
    }

    public Student registerStudent(Student student, String rawPassword) {
        if (student.getUser() == null) {
            String email = student.getRegistrationNumber().toLowerCase().replace("/", "") + "@campus.ac.ug";
            User user = userService.createUser(
                    student.getRegistrationNumber(),
                    rawPassword != null && !rawPassword.isEmpty() ? rawPassword : "password123",
                    email,
                    Role.ROLE_STUDENT
            );
            student.setUser(user);
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public long getTotalStudentCount() {
        return studentRepository.count();
    }

    // --- Course Registration operations ---
    public CourseRegistration registerCourse(Student student, Course course, AcademicYear academicYear, Semester semester) {
        if (courseRegistrationRepository.existsByStudentAndCourseAndAcademicYearAndSemester(student, course, academicYear, semester)) {
            throw new IllegalStateException("Student is already registered for this course in the current term.");
        }
        CourseRegistration reg = new CourseRegistration(student, course, academicYear, semester);
        return courseRegistrationRepository.save(reg);
    }

    public void dropCourse(Long registrationId) {
        courseRegistrationRepository.deleteById(registrationId);
    }

    public List<CourseRegistration> getRegistrationsForStudent(Student student) {
        return courseRegistrationRepository.findByStudent(student);
    }

    public List<CourseRegistration> getRegistrationsForStudentInTerm(Student student, AcademicYear academicYear, Semester semester) {
        return courseRegistrationRepository.findByStudentAndAcademicYearAndSemester(student, academicYear, semester);
    }

    public List<CourseRegistration> getRegistrationsForCourseInTerm(Course course, AcademicYear academicYear, Semester semester) {
        return courseRegistrationRepository.findByCourseAndAcademicYearAndSemester(course, academicYear, semester);
    }

    public Optional<CourseRegistration> getRegistrationById(Long id) {
        return courseRegistrationRepository.findById(id);
    }

    public long getEnrollmentCountForCourse(Course course, AcademicYear academicYear, Semester semester) {
        return courseRegistrationRepository.countByCourseAndAcademicYearAndSemester(course, academicYear, semester);
    }
}
