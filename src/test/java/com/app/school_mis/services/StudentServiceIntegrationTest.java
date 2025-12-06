package com.app.school_mis.services;

import com.app.school_mis.model.Student;
import com.app.school_mis.repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentService.
 * 
 * This test class demonstrates service layer integration testing by:
 * - Using @SpringBootTest to load the full application context
 * - Testing service methods with real repository interactions (not mocked)
 * - Verifying the service layer correctly orchestrates repository operations
 * - Using @Transactional to ensure test isolation (each test rolls back)
 * 
 * Key differences from repository tests:
 * - @SpringBootTest loads the entire application context (not just JPA
 * components)
 * - Tests the service layer's business logic and its interaction with the
 * repository
 * - Demonstrates integration between multiple application layers
 * 
 * @SpringBootTest provides:
 *                 - Full Spring application context
 *                 - All beans and components are loaded
 *                 - Real service-repository integration
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // Each test runs in a transaction that's rolled back after completion
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Clean up the database before each test to ensure test isolation.
     * This ensures each test starts with a clean slate.
     */
    @BeforeEach
    void setUp() {
        // Given: Clean database state before each test
        studentRepository.deleteAll();
    }

    /**
     * Test: Verify that the service can save a student through the repository.
     * 
     * This test demonstrates:
     * - Service layer calling repository to persist data
     * - Integration between service and repository layers
     * - Verification that saved data is correctly stored in the database
     */
    @Test
    void whenSaveStudent_thenStudentIsPersistedThroughService() {
        // Given: Create a new student
        Student student = new Student("Jane Doe", "jane.doe@example.com");

        // When: Save the student using the service
        Student savedStudent = studentService.save(student);

        // Then: Verify the student was saved successfully
        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getName()).isEqualTo("Jane Doe");
        assertThat(savedStudent.getEmail()).isEqualTo("jane.doe@example.com");

        // Additional verification: Check the student exists in the database
        Student foundStudent = studentRepository.findById(savedStudent.getId()).orElse(null);
        assertThat(foundStudent).isNotNull();
        assertThat(foundStudent.getName()).isEqualTo("Jane Doe");
    }

    /**
     * Test: Verify that the service can retrieve all students from the repository.
     * 
     * This test demonstrates:
     * - Service layer delegating to repository for data retrieval
     * - Handling multiple records through the service layer
     * - Integration testing with pre-populated test data
     */
    @Test
    void whenGetAll_thenReturnAllStudentsThroughService() {
        // Given: Save multiple students directly to the repository
        Student student1 = new Student("Alice Johnson", "alice.j@example.com");
        Student student2 = new Student("Bob Williams", "bob.w@example.com");
        Student student3 = new Student("Charlie Davis", "charlie.d@example.com");

        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);

        // When: Retrieve all students using the service
        List<Student> students = studentService.getAll();

        // Then: Verify all students are retrieved
        assertThat(students).isNotNull();
        assertThat(students).hasSize(3);
        assertThat(students).extracting(Student::getName)
                .containsExactlyInAnyOrder("Alice Johnson", "Bob Williams", "Charlie Davis");
    }

    /**
     * Test: Verify that the service returns an empty list when no students exist.
     * 
     * This test demonstrates:
     * - Service layer handling edge cases (empty database)
     * - Proper delegation to repository for empty results
     */
    @Test
    void whenGetAllWithNoStudents_thenReturnEmptyList() {
        // Given: Empty database (setUp() already cleared it)

        // When: Retrieve all students using the service
        List<Student> students = studentService.getAll();

        // Then: Verify the list is empty
        assertThat(students).isNotNull();
        assertThat(students).isEmpty();
    }

    /**
     * Test: Verify that the service can delete a student through the repository.
     * 
     * This test demonstrates:
     * - Service layer orchestrating delete operations
     * - Integration between service and repository for deletions
     * - Verification that data is actually removed from the database
     */
    @Test
    void whenDeleteStudent_thenStudentIsRemovedThroughService() {
        // Given: Save a student to the database
        Student student = new Student("Michael Brown", "michael.b@example.com");
        Student savedStudent = studentRepository.save(student);
        Long studentId = savedStudent.getId();

        // Verify the student exists before deletion
        assertThat(studentRepository.findById(studentId)).isPresent();

        // When: Delete the student using the service
        studentService.delete(studentId);

        // Then: Verify the student no longer exists in the database
        assertThat(studentRepository.findById(studentId)).isNotPresent();
    }

    /**
     * Test: Verify that saving multiple students through the service works
     * correctly.
     * 
     * This test demonstrates:
     * - Service layer handling multiple save operations
     * - Verifying each operation is independent and successful
     * - Integration testing with sequential operations
     */
    @Test
    void whenSaveMultipleStudents_thenAllArePersistedThroughService() {
        // Given: Create multiple students
        Student student1 = new Student("Sarah Miller", "sarah.m@example.com");
        Student student2 = new Student("Tom Anderson", "tom.a@example.com");

        // When: Save students using the service
        Student saved1 = studentService.save(student1);
        Student saved2 = studentService.save(student2);

        // Then: Verify both students are saved with unique IDs
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());

        // Verify both students exist in the database
        List<Student> allStudents = studentService.getAll();
        assertThat(allStudents).hasSize(2);
        assertThat(allStudents).extracting(Student::getName)
                .containsExactlyInAnyOrder("Sarah Miller", "Tom Anderson");
    }

    /**
     * Test: Verify the complete lifecycle of a student (create, retrieve, delete).
     * 
     * This test demonstrates:
     * - End-to-end integration testing through the service layer
     * - Multiple service operations in sequence
     * - Verification of state changes at each step
     */
    @Test
    void testCompleteStudentLifecycle() {
        // Given: Start with empty database
        assertThat(studentService.getAll()).isEmpty();

        // When: Create a new student
        Student student = new Student("Emily Taylor", "emily.t@example.com");
        Student savedStudent = studentService.save(student);
        Long studentId = savedStudent.getId();

        // Then: Verify student is created
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(studentService.getAll()).hasSize(1);

        // When: Retrieve all students
        List<Student> students = studentService.getAll();

        // Then: Verify the student is in the list
        assertThat(students).hasSize(1);
        assertThat(students.get(0).getName()).isEqualTo("Emily Taylor");

        // When: Delete the student
        studentService.delete(studentId);

        // Then: Verify the database is empty again
        assertThat(studentService.getAll()).isEmpty();
    }
}
