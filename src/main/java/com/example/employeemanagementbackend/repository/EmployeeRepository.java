package com.example.employeemanagementbackend.repository;

import com.example.employeemanagementbackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Check if department has active employees
    boolean existsByDepartmentIdAndActive(Long departmentId, boolean active);

    // Combined search + filter
    @Query("SELECT e FROM Employee e WHERE e.active = true " +
            "AND (:name IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:departmentId IS NULL OR e.department.id = :departmentId)")
    List<Employee> searchAndFilter(
            @Param("name") String name,
            @Param("departmentId") Long departmentId);
}