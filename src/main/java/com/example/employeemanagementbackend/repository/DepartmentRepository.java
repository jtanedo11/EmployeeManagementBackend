package com.example.employeemanagementbackend.repository;

import com.example.employeemanagementbackend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    //Search for Department
    Optional<Department> findByName(String name);
}
