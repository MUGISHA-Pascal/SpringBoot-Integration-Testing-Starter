package com.app.school_mis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.school_mis.model.Student;

public interface StudentRepository extends JpaRepository<Student,Long>{}