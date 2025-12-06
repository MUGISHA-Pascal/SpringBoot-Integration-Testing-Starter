package com.app.school_mis.controllers;

import com.app.school_mis.model.Student;
import org.springframework.web.bind.annotation.*;
import com.app.school_mis.services.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/all")
    public List<Student> getAllStudents() {
        return studentService.getAll();
    }

    @PostMapping("/create")
    public Student createStudent(@RequestBody Student student) {
        return studentService.save(student);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
    }

}
