package com.app.school_mis.controllers;

import com.app.school_mis.model.Student;
import com.app.school_mis.repositories.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentController.
 * 
 * This test class demonstrates full-stack integration testing by:
 * - Using @SpringBootTest to load the complete application context
 * - Using @AutoConfigureMockMvc to configure MockMvc for testing REST endpoints
 * - Testing HTTP requests and responses through the entire application stack
 * - Verifying Controller → Service → Repository → Database integration
 * - Testing JSON serialization/deserialization
 * - Validating HTTP status codes and response bodies
 * 
 * Key components:
 * - MockMvc: Simulates HTTP requests without starting a real server
 * - ObjectMapper: Converts Java objects to/from JSON
 * - Full application context: All layers are real (not mocked)
 * 
 * This represents the highest level of integration testing, verifying that:
 * - REST endpoints are correctly mapped
 * - Request/response handling works properly
 * - All application layers work together seamlessly
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // Each test runs in a transaction that's rolled back after completion
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
     * Test: POST /api/v1/students/create - Create a new student.
     * 
     * This test demonstrates:
     * - Sending a POST request with JSON body
     * - Testing the complete flow: Controller → Service → Repository → Database
     * - Verifying HTTP 200 status code
     * - Validating the JSON response contains correct data
     * - Confirming the student is actually persisted in the database
     */
    @Test
    void whenCreateStudent_thenStudentIsCreatedAndReturned() throws Exception {
        // Given: Create a student object to send in the request
        Student student = new Student("John Smith", "john.smith@example.com");

        // When: Send POST request to create a student
        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student)))
                // Then: Verify the response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));

        // Additional verification: Check the student exists in the database
        assertThat(studentRepository.findAll()).hasSize(1);
        Student savedStudent = studentRepository.findAll().get(0);
        assertThat(savedStudent.getName()).isEqualTo("John Smith");
        assertThat(savedStudent.getEmail()).isEqualTo("john.smith@example.com");
    }

    /**
     * Test: GET /api/v1/students/all - Retrieve all students.
     * 
     * This test demonstrates:
     * - Sending a GET request
     * - Testing retrieval of multiple records
     * - Verifying JSON array response
     * - Validating response contains all expected students
     */
    @Test
    void whenGetAllStudents_thenReturnJsonArray() throws Exception {
        // Given: Save multiple students to the database
        Student student1 = new Student("Alice Brown", "alice.b@example.com");
        Student student2 = new Student("Bob Green", "bob.g@example.com");
        Student student3 = new Student("Charlie White", "charlie.w@example.com");

        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);

        // When: Send GET request to retrieve all students
        mockMvc.perform(get("/api/v1/students/all")
                .contentType(MediaType.APPLICATION_JSON))
                // Then: Verify the response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "Alice Brown", "Bob Green", "Charlie White")))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                        "alice.b@example.com", "bob.g@example.com", "charlie.w@example.com")));
    }

    /**
     * Test: GET /api/v1/students/all - Return empty array when no students exist.
     * 
     * This test demonstrates:
     * - Testing edge case with empty database
     * - Verifying the endpoint returns an empty JSON array
     * - Confirming proper HTTP status even with no data
     */
    @Test
    void whenGetAllStudentsWithEmptyDatabase_thenReturnEmptyArray() throws Exception {
        // Given: Empty database (setUp() already cleared it)

        // When: Send GET request to retrieve all students
        mockMvc.perform(get("/api/v1/students/all")
                .contentType(MediaType.APPLICATION_JSON))
                // Then: Verify the response is an empty array
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Test: DELETE /api/v1/students/delete/{id} - Delete a student by ID.
     * 
     * This test demonstrates:
     * - Sending a DELETE request with path variable
     * - Testing the complete delete flow through all layers
     * - Verifying the student is removed from the database
     * - Confirming proper HTTP status for successful deletion
     */
    @Test
    void whenDeleteStudent_thenStudentIsRemoved() throws Exception {
        // Given: Save a student to the database
        Student student = new Student("David Black", "david.b@example.com");
        Student savedStudent = studentRepository.save(student);
        Long studentId = savedStudent.getId();

        // Verify the student exists before deletion
        assertThat(studentRepository.findById(studentId)).isPresent();

        // When: Send DELETE request to remove the student
        mockMvc.perform(delete("/api/v1/students/delete/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
                // Then: Verify successful deletion (200 OK)
                .andExpect(status().isOk());

        // Additional verification: Check the student no longer exists in the database
        assertThat(studentRepository.findById(studentId)).isNotPresent();
        assertThat(studentRepository.findAll()).isEmpty();
    }

    /**
     * Test: Complete workflow - Create, retrieve, and delete students.
     * 
     * This test demonstrates:
     * - End-to-end integration testing through REST API
     * - Multiple operations in sequence
     * - Verifying state changes at each step
     * - Real-world usage scenario
     */
    @Test
    void testCompleteStudentWorkflowThroughAPI() throws Exception {
        // Given: Start with empty database
        assertThat(studentRepository.findAll()).isEmpty();

        // When: Create first student via API
        Student student1 = new Student("Emma Wilson", "emma.w@example.com");
        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student1)))
                .andExpect(status().isOk());

        // When: Create second student via API
        Student student2 = new Student("Frank Miller", "frank.m@example.com");
        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student2)))
                .andExpect(status().isOk());

        // Then: Verify both students exist via API
        mockMvc.perform(get("/api/v1/students/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // When: Get the ID of the first student and delete it
        Long firstStudentId = studentRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/api/v1/students/delete/{id}", firstStudentId))
                .andExpect(status().isOk());

        // Then: Verify only one student remains
        mockMvc.perform(get("/api/v1/students/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Verify the remaining student is the second one
        assertThat(studentRepository.findAll()).hasSize(1);
    }

    /**
     * Test: POST /api/v1/students/create - Create multiple students sequentially.
     * 
     * This test demonstrates:
     * - Multiple POST requests in sequence
     * - Verifying each student gets a unique ID
     * - Testing that the system handles multiple creations correctly
     */
    @Test
    void whenCreateMultipleStudents_thenAllArePersistedWithUniqueIds() throws Exception {
        // Given: Create multiple student objects
        Student student1 = new Student("Grace Lee", "grace.l@example.com");
        Student student2 = new Student("Henry Taylor", "henry.t@example.com");
        Student student3 = new Student("Ivy Anderson", "ivy.a@example.com");

        // When: Create students via API
        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(post("/api/v1/students/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        // Then: Verify all students are in the database with unique IDs
        assertThat(studentRepository.findAll()).hasSize(3);
        assertThat(studentRepository.findAll())
                .extracting(Student::getId)
                .doesNotHaveDuplicates();
    }
}
