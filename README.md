# Spring Boot Integration Testing Starter

A comprehensive demonstration project showcasing **integration testing** best practices in Spring Boot applications using JUnit 5, Mockito, and AssertJ.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Endpoints](#api-endpoints)
- [Integration Testing Strategy](#integration-testing-strategy)
- [Database Configuration](#database-configuration)
- [Key Features](#key-features)

## Overview

This project is a **School Management Information System (MIS)** starter that demonstrates how to write effective integration tests for a Spring Boot REST API. Unlike unit tests that mock dependencies, integration tests verify that all application layers work together correctly.

### What is Integration Testing?

Integration testing validates that different components of your application work together as expected:

- **Controller** → **Service** → **Repository** → **Database**
- HTTP request/response handling
- JSON serialization/deserialization
- Database transactions
- Complete application context

## Architecture

The application follows a standard **3-tier architecture**:

```
┌─────────────────────────────────────────┐
│         REST Controller Layer           │
│    (StudentController.java)             │
│    - Handles HTTP requests              │
│    - Maps endpoints                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│    (StudentService.java)                │
│    - Business logic                     │
│    - Transaction management             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │
│    (StudentRepository.java)             │
│    - Data access                        │
│    - JPA operations                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Database                        │
│    Production: PostgreSQL               │
│    Testing: H2 (in-memory)              │
└─────────────────────────────────────────┘
```

## Technologies

### Core Framework

- **Spring Boot 3.5.7** - Application framework
- **Java 17** - Programming language
- **Maven** - Build tool

### Database

- **PostgreSQL** - Production database
- **H2 Database** - In-memory database for testing
- **Spring Data JPA** - Data access layer

### Testing

- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **MockMvc** - REST API testing
- **@SpringBootTest** - Full application context testing

### Additional Libraries

- **Lombok** - Reduces boilerplate code
- **Spring Boot DevTools** - Development utilities
- **Spring Boot Actuator** - Production-ready features

## Project Structure

```
SpringBoot-Integration-Testing-Starter/
├── src/
│   ├── main/
│   │   ├── java/com/app/school_mis/
│   │   │   ├── SchoolMisApplication.java          # Main application class
│   │   │   ├── controllers/
│   │   │   │   └── StudentController.java         # REST endpoints
│   │   │   ├── model/
│   │   │   │   └── Student.java                   # JPA entity
│   │   │   ├── repositories/
│   │   │   │   └── StudentRepository.java         # Data access
│   │   │   └── services/
│   │   │       └── StudentService.java            # Business logic
│   │   └── resources/
│   │       └── application.properties             # Production config
│   └── test/
│       ├── java/com/app/school_mis/
│       │   ├── SchoolMisApplicationTests.java     # Basic context test
│       │   ├── controllers/
│       │   │   └── StudentControllerIntegrationTest.java    # Controller tests
│       │   ├── repositories/
│       │   │   └── StudentRepositoryIntegrationTest.java    # Repository tests
│       │   └── services/
│       │       └── StudentServiceIntegrationTest.java       # Service tests
│       └── resources/
│           └── application-test.properties        # Test configuration
├── pom.xml                                        # Maven dependencies
└── README.md                                      # This file
```

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **PostgreSQL 12+** (for production mode)

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd SpringBoot-Integration-Testing-Starter
   ```

2. **Configure PostgreSQL** (for production)

   Create a database:

   ```sql
   CREATE DATABASE school_mis;
   ```

3. **Update database credentials** (if needed)

   Edit `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/school_mis
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Build the project**
   ```bash
   ./mvnw clean install
   ```

## Running the Application

### Development Mode

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Production Mode

```bash
./mvnw clean package
java -jar target/School-MIS-0.0.1-SNAPSHOT.jar
```

## Running Tests

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=StudentControllerIntegrationTest
```

### Run with Coverage

```bash
./mvnw clean test jacoco:report
```

### Test Configuration

Tests use **H2 in-memory database** instead of PostgreSQL for:

- Fast execution
- Isolated test environment
- No external dependencies
- Automatic cleanup

Configuration: `src/test/resources/application-test.properties`

## API Endpoints

### Base URL

```
http://localhost:8080/api/v1/students
```

### Endpoints

| Method   | Endpoint       | Description      | Request Body   | Response        |
| -------- | -------------- | ---------------- | -------------- | --------------- |
| `GET`    | `/all`         | Get all students | -              | `List<Student>` |
| `POST`   | `/create`      | Create a student | `Student` JSON | `Student`       |
| `DELETE` | `/delete/{id}` | Delete a student | -              | `void`          |

### Student Model

```json
{
  "id": 1,
  "name": "John Smith",
  "email": "john.smith@example.com"
}
```

### Example API Calls

#### Create a Student

```bash
curl -X POST http://localhost:8080/api/v1/students/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com"
  }'
```

#### Get All Students

```bash
curl http://localhost:8080/api/v1/students/all
```

#### Delete a Student

```bash
curl -X DELETE http://localhost:8080/api/v1/students/delete/1
```

## Integration Testing Strategy

### Test Layers

This project includes integration tests for all three layers:

#### 1. **Controller Integration Tests** (`StudentControllerIntegrationTest.java`)

Tests the complete HTTP request/response cycle:

- REST endpoint mapping
- JSON serialization/deserialization
- HTTP status codes
- Full stack integration (Controller → Service → Repository → Database)

**Key Annotations:**

```java
@SpringBootTest                    // Load full application context
@AutoConfigureMockMvc              // Configure MockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional                     // Rollback after each test
```

**Example Test:**

```java
@Test
void whenCreateStudent_thenStudentIsCreatedAndReturned() throws Exception {
    Student student = new Student("John Smith", "john.smith@example.com");

    mockMvc.perform(post("/api/v1/students/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(student)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Smith"));
}
```

#### 2. **Service Integration Tests** (`StudentServiceIntegrationTest.java`)

Tests business logic with real database:

- Service methods
- Transaction management
- Database persistence
- Service → Repository → Database integration

**Key Annotations:**

```java
@SpringBootTest                    // Load application context
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional                     // Rollback after each test
```

#### 3. **Repository Integration Tests** (`StudentRepositoryIntegrationTest.java`)

Tests data access layer:

- JPA operations
- Custom queries
- Database constraints
- Repository → Database integration

**Key Annotations:**

```java
@DataJpaTest                       // Configure JPA components only
@AutoConfigureTestDatabase(replace = NONE)  // Use test database
```

### Testing Best Practices Demonstrated

1. **Test Isolation**: Each test starts with a clean database state
2. **Given-When-Then**: Clear test structure for readability
3. **Comprehensive Coverage**: Tests for success, edge cases, and workflows
4. **Real Dependencies**: No mocking of infrastructure (database, JPA)
5. **Transactional Tests**: Automatic rollback prevents test pollution
6. **Descriptive Names**: Test method names clearly describe what is tested

### Test Examples

The project includes tests for:

- Creating students
- Retrieving all students
- Deleting students
- Empty database scenarios
- Multiple operations workflows
- Unique ID generation
- Complete CRUD workflows

## Database Configuration

### Production (PostgreSQL)

**File:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/school_mis
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Testing (H2 In-Memory)

**File:** `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

**Why H2 for Testing?**

- Fast - In-memory database
- Isolated - Fresh database for each test run
- Embedded - No external dependencies
- Clean - Automatic cleanup with `create-drop`

## Key Features

### Application Features

- RESTful API design
- CRUD operations for Student entity
- PostgreSQL integration
- JPA/Hibernate ORM
- Lombok for clean code
- Spring Boot DevTools for development

### Testing Features

- Full integration test coverage
- MockMvc for REST API testing
- H2 in-memory database for tests
- Transactional test isolation
- AssertJ fluent assertions
- Comprehensive test documentation
- Given-When-Then test structure

## Learning Resources

### What You'll Learn

1. **Integration Testing Fundamentals**

   - Difference between unit and integration tests
   - When to use integration tests
   - Test isolation strategies

2. **Spring Boot Testing**

   - `@SpringBootTest` vs `@DataJpaTest`
   - MockMvc for REST API testing
   - Test property sources
   - Transactional testing

3. **Best Practices**
   - Test naming conventions
   - Given-When-Then structure
   - Database test configuration
   - Test data management

### Code Documentation

All test classes include extensive comments explaining:

- What is being tested
- Why the test is important
- How the test works
- Key concepts demonstrated

## Contributing

This is a learning project. Feel free to:

- Add more test scenarios
- Extend the Student entity
- Add new endpoints
- Improve documentation

## License

This project is open source and available for educational purposes.

## Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)

---

**Happy Testing!**
