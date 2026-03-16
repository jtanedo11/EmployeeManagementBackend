package com.example.employeemanagementbackend.repository;

import com.example.employeemanagementbackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Search by employeeId
    Optional<Employee> findByEmployeeId(Long employeeId);

    // Search by name (case-insensitive, partial match)
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // List all active employees by department
    List<Employee> findByDepartmentIdAndActive(Long departmentId, boolean active);

    // List all active employees ordered by date of birth (age)
    List<Employee> findByActiveOrderByDateOfBirthAsc(boolean active);

    // Get all active employees only
    List<Employee> findByActive(boolean active);
}
