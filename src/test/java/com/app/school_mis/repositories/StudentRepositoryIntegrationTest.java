package com.app.school_mis.repositories;

import com.app.school_mis.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentRepository.
 * 
 * This test class demonstrates repository layer integration testing by:
 * - Using @DataJpaTest to load only JPA components and configure an in-memory
 * database
 * - Testing actual database interactions without mocking
 * - Verifying CRUD operations work correctly with the database
 * - Using TestEntityManager for test data setup and verification
 * 
 * @DataJpaTest provides:
 *              - Automatic configuration of H2 in-memory database
 *              - Transaction management (each test runs in a transaction that's
 *              rolled back)
 *              - JPA entity scanning and repository configuration
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class StudentRepositoryIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Test: Verify that a student can be saved to the database and retrieved.
     * 
     * This test demonstrates:
     * - Creating a new Student entity
     * - Persisting it using the repository
     * - Verifying the student is saved with an auto-generated ID
     * - Confirming the saved data matches the input
     */
    @Test
    void whenSaveStudent_thenStudentIsPersisted() {
        // Given: Create a new student with name and email
        Student student = new Student("John Doe", "john.doe@example.com");

        // When: Save the student using the repository
        Student savedStudent = studentRepository.save(student);

        // Then: Verify the student was saved successfully
        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getId()).isNotNull(); // ID should be auto-generated
        assertThat(savedStudent.getName()).isEqualTo("John Doe");
        assertThat(savedStudent.getEmail()).isEqualTo("john.doe@example.com");
    }

    /**
     * Test: Verify that findAll() retrieves all students from the database.
     * 
     * This test demonstrates:
     * - Setting up test data using TestEntityManager
     * - Retrieving all records using repository's findAll() method
     * - Verifying the correct number of records and their content
     */
    @Test
    void whenFindAll_thenReturnAllStudents() {
        // Given: Persist multiple students directly to the database
        Student student1 = new Student("Alice Smith", "alice@example.com");
        Student student2 = new Student("Bob Johnson", "bob@example.com");
        Student student3 = new Student("Charlie Brown", "charlie@example.com");

        entityManager.persist(student1);
        entityManager.persist(student2);
        entityManager.persist(student3);
        entityManager.flush(); // Ensure data is written to the database

        // When: Retrieve all students using the repository
        List<Student> students = studentRepository.findAll();

        // Then: Verify all students are retrieved
        assertThat(students).isNotNull();
        assertThat(students).hasSize(3);
        assertThat(students).extracting(Student::getName)
                .containsExactlyInAnyOrder("Alice Smith", "Bob Johnson", "Charlie Brown");
    }

    /**
     * Test: Verify that a student can be found by their ID.
     * 
     * This test demonstrates:
     * - Persisting a student and obtaining their ID
     * - Finding the student by ID using repository's findById() method
     * - Verifying the retrieved student matches the persisted data
     */
    @Test
    void whenFindById_thenReturnStudent() {
        // Given: Persist a student and get their ID
        Student student = new Student("David Lee", "david@example.com");
        Student persistedStudent = entityManager.persist(student);
        entityManager.flush();
        Long studentId = persistedStudent.getId();

        // When: Find the student by ID
        Optional<Student> foundStudent = studentRepository.findById(studentId);

        // Then: Verify the student is found and data is correct
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getName()).isEqualTo("David Lee");
        assertThat(foundStudent.get().getEmail()).isEqualTo("david@example.com");
    }

    /**
     * Test: Verify that a student can be deleted from the database.
     * 
     * This test demonstrates:
     * - Persisting a student
     * - Deleting the student using repository's deleteById() method
     * - Verifying the student no longer exists in the database
     */
    @Test
    void whenDeleteById_thenStudentIsRemoved() {
        // Given: Persist a student
        Student student = new Student("Emma Wilson", "emma@example.com");
        Student persistedStudent = entityManager.persist(student);
        entityManager.flush();
        Long studentId = persistedStudent.getId();

        // When: Delete the student by ID
        studentRepository.deleteById(studentId);
        entityManager.flush();

        // Then: Verify the student no longer exists
        Optional<Student> deletedStudent = studentRepository.findById(studentId);
        assertThat(deletedStudent).isNotPresent();
    }

    /**
     * Test: Verify that findAll() returns an empty list when no students exist.
     * 
     * This test demonstrates:
     * - Testing edge case with empty database
     * - Verifying repository handles empty results correctly
     */
    @Test
    void whenNoStudentsExist_thenFindAllReturnsEmptyList() {
        // Given: No students in the database (clean state)

        // When: Retrieve all students
        List<Student> students = studentRepository.findAll();

        // Then: Verify the list is empty
        assertThat(students).isNotNull();
        assertThat(students).isEmpty();
    }

    /**
     * Test: Verify that findById() returns empty Optional for non-existent ID.
     * 
     * This test demonstrates:
     * - Testing edge case with invalid ID
     * - Verifying repository returns Optional.empty() for non-existent records
     */
    @Test
    void whenFindByNonExistentId_thenReturnEmpty() {
        // Given: An ID that doesn't exist in the database
        Long nonExistentId = 999L;

        // When: Try to find student by non-existent ID
        Optional<Student> foundStudent = studentRepository.findById(nonExistentId);

        // Then: Verify Optional is empty
        assertThat(foundStudent).isNotPresent();
    }
}
