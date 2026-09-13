package com.smartcampus.config;

import com.smartcampus.model.*;
import com.smartcampus.repository.*;
import com.smartcampus.service.GradingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final GradingService gradingService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           ProgramRepository programRepository,
                           AcademicYearRepository academicYearRepository,
                           SemesterRepository semesterRepository,
                           CourseRepository courseRepository,
                           LecturerRepository lecturerRepository,
                           StudentRepository studentRepository,
                           CourseAssignmentRepository courseAssignmentRepository,
                           CourseRegistrationRepository courseRegistrationRepository,
                           AttendanceRepository attendanceRepository,
                           GradeRepository gradeRepository,
                           GradingService gradingService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.courseRegistrationRepository = courseRegistrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.gradeRepository = gradeRepository;
        this.gradingService = gradingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        System.out.println(">>> Initializing Smart Campus demo data...");

        // 1. Create Admin
        User adminUser = new User("admin", passwordEncoder.encode("admin123"), "admin@campus.ac.ug", Role.ROLE_ADMIN);
        userRepository.save(adminUser);

        // 2. Create Departments
        Department csDept = departmentRepository.save(new Department("CS", "Computer Science & Software Engineering", "Department of Computing Sciences"));
        Department eeDept = departmentRepository.save(new Department("EE", "Electrical & Electronic Engineering", "Department of Engineering"));
        Department bisDept = departmentRepository.save(new Department("BIS", "Business Information Systems", "Department of Business Computing"));

        // 3. Create Programs
        Program bcs = programRepository.save(new Program("BCS", "Bachelor of Science in Computer Science", csDept, 3, "Bachelor"));
        Program bse = programRepository.save(new Program("BSE", "Bachelor of Science in Software Engineering", csDept, 4, "Bachelor"));
        Program bbit = programRepository.save(new Program("BBIT", "Bachelor of Business Information Technology", bisDept, 3, "Bachelor"));

        // 4. Create Academic Year & Semesters
        AcademicYear ay2025 = academicYearRepository.save(new AcademicYear("2025/2026", true));
        Semester sem1 = semesterRepository.save(new Semester("Semester 1", ay2025, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 31), true));
        Semester sem2 = semesterRepository.save(new Semester("Semester 2", ay2025, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 6, 30), false));

        // 5. Create Courses
        Course cs101 = courseRepository.save(new Course("CS101", "Introduction to Computer Science", 3, csDept, bcs, 1, 1, "Foundations of computing, logic, and hardware basics."));
        Course cs102 = courseRepository.save(new Course("CS102", "Data Structures & Algorithms", 4, csDept, bcs, 1, 1, "Arrays, linked lists, trees, graphs, and algorithmic complexity."));
        Course cs103 = courseRepository.save(new Course("CS103", "Database Systems", 3, csDept, bcs, 1, 1, "Relational databases, SQL, ER modeling, and indexing."));
        Course cs104 = courseRepository.save(new Course("CS104", "Web Application Development", 4, csDept, bcs, 1, 1, "Full-stack development using Java Spring Boot, HTML5, and Bootstrap."));
        Course cs105 = courseRepository.save(new Course("CS105", "Computer Networks", 3, csDept, bcs, 2, 1, "OSI model, TCP/IP protocol suite, routing, and network security."));

        // 6. Create Lecturers
        User lectUser1 = userRepository.save(new User("dr.smith", passwordEncoder.encode("password123"), "dr.smith@campus.ac.ug", Role.ROLE_LECTURER));
        Lecturer lect1 = lecturerRepository.save(new Lecturer("LEC001", lectUser1, "Dr. Alan Smith", "Dr.", "dr.smith@campus.ac.ug", "+256-700-111222", csDept, "Block B, Room 204"));

        User lectUser2 = userRepository.save(new User("prof.johnson", passwordEncoder.encode("password123"), "prof.johnson@campus.ac.ug", Role.ROLE_LECTURER));
        Lecturer lect2 = lecturerRepository.save(new Lecturer("LEC002", lectUser2, "Prof. Sarah Johnson", "Prof.", "prof.johnson@campus.ac.ug", "+256-700-333444", csDept, "Block B, Room 310"));

        // 7. Assign Lecturers to Courses
        courseAssignmentRepository.save(new CourseAssignment(cs102, lect1, ay2025, sem1));
        courseAssignmentRepository.save(new CourseAssignment(cs104, lect1, ay2025, sem1));
        courseAssignmentRepository.save(new CourseAssignment(cs101, lect2, ay2025, sem1));
        courseAssignmentRepository.save(new CourseAssignment(cs103, lect2, ay2025, sem1));

        // 8. Create Students
        User studUser1 = userRepository.save(new User("std001", passwordEncoder.encode("password123"), "std001@campus.ac.ug", Role.ROLE_STUDENT));
        Student student1 = studentRepository.save(new Student(
                "STD/2025/001", studUser1, "John Mark Doe", "Male",
                LocalDate.of(2003, 5, 14), "+256-750-123456", "Main Campus Hall 3",
                bcs, 1, 1, "ACTIVE"
        ));

        User studUser2 = userRepository.save(new User("std002", passwordEncoder.encode("password123"), "std002@campus.ac.ug", Role.ROLE_STUDENT));
        Student student2 = studentRepository.save(new Student(
                "STD/2025/002", studUser2, "Alice Mary Smith", "Female",
                LocalDate.of(2004, 2, 20), "+256-750-987654", "Mary Stuart Hall",
                bcs, 1, 1, "ACTIVE"
        ));

        // 9. Register Courses for Students
        CourseRegistration reg1 = courseRegistrationRepository.save(new CourseRegistration(student1, cs101, ay2025, sem1));
        CourseRegistration reg2 = courseRegistrationRepository.save(new CourseRegistration(student1, cs102, ay2025, sem1));
        CourseRegistration reg3 = courseRegistrationRepository.save(new CourseRegistration(student1, cs103, ay2025, sem1));
        CourseRegistration reg4 = courseRegistrationRepository.save(new CourseRegistration(student1, cs104, ay2025, sem1));

        CourseRegistration reg5 = courseRegistrationRepository.save(new CourseRegistration(student2, cs101, ay2025, sem1));
        CourseRegistration reg6 = courseRegistrationRepository.save(new CourseRegistration(student2, cs102, ay2025, sem1));
        CourseRegistration reg7 = courseRegistrationRepository.save(new CourseRegistration(student2, cs103, ay2025, sem1));

        // 10. Record Sample Attendance
        LocalDate today = LocalDate.now();
        attendanceRepository.save(new Attendance(cs102, student1, today.minusDays(7), Attendance.Status.PRESENT, "Lecture 1: Arrays & Pointers", lect1));
        attendanceRepository.save(new Attendance(cs102, student2, today.minusDays(7), Attendance.Status.PRESENT, "Lecture 1: Arrays & Pointers", lect1));
        attendanceRepository.save(new Attendance(cs102, student1, today.minusDays(3), Attendance.Status.PRESENT, "Lecture 2: Singly Linked Lists", lect1));
        attendanceRepository.save(new Attendance(cs102, student2, today.minusDays(3), Attendance.Status.LATE, "Lecture 2: Singly Linked Lists", lect1));
        attendanceRepository.save(new Attendance(cs104, student1, today.minusDays(2), Attendance.Status.PRESENT, "Lecture 1: Spring Boot MVC Architecture", lect1));

        // 11. Enter Grades
        gradingService.recordMarks(reg1, 36.0, 52.0, lect2); // Total 88 (A, 5.0)
        gradingService.recordMarks(reg2, 34.0, 48.0, lect1); // Total 82 (A, 5.0)
        gradingService.recordMarks(reg3, 30.0, 45.0, lect2); // Total 75 (B, 4.0)
        gradingService.recordMarks(reg4, 38.0, 55.0, lect1); // Total 93 (A, 5.0)

        gradingService.recordMarks(reg5, 28.0, 42.0, lect2); // Total 70 (B, 4.0)
        gradingService.recordMarks(reg6, 31.0, 46.0, lect1); // Total 77 (B, 4.0)
        gradingService.recordMarks(reg7, 25.0, 38.0, lect2); // Total 63 (C, 3.0)

        System.out.println(">>> Smart Campus demo data initialized successfully!");
        System.out.println(">>> Admin: admin / admin123");
        System.out.println(">>> Lecturer: dr.smith / password123, prof.johnson / password123");
        System.out.println(">>> Student: std001 / password123, std002 / password123");
    }
}
