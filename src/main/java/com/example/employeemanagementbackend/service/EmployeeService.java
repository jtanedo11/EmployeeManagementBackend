package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.EmployeeRequestDTO;
import com.example.employeemanagementbackend.dto.EmployeeResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.entity.Employee;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    // ── ADD ──────────────────────────────────────────────────────────
    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO request) {

        // Check if employeeId already exists
        if (employeeRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
            throw new DuplicateEntryException("Employee ID already exists: " + request.getEmployeeId());
        }

        // Find department or throw exception
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Map DTO → Entity
        Employee employee = new Employee();
        employee.setEmployeeId(request.getEmployeeId());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setDepartment(department);
        employee.setSalary(request.getSalary());
        employee.setActive(true);

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    // ── UPDATE ───────────────────────────────────────────────────────
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO request) {

        // Find existing employee
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check if new employeeId is taken by another employee
        employeeRepository.findByEmployeeId(request.getEmployeeId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateEntryException("Employee ID already exists: " + request.getEmployeeId());
                    }
                });

        // Find department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Update fields
        employee.setEmployeeId(request.getEmployeeId());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setDepartment(department);
        employee.setSalary(request.getSalary());

        Employee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    // ── GET BY ID ────────────────────────────────────────────────────
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    // ── SOFT DELETE ──────────────────────────────────────────────────
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Soft delete — just set active to false
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    // ── GET ALL (active + inactive) ───────────────────────────────────
    public List<EmployeeResponseDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponseDTO> response = new ArrayList<>();
        for (Employee employee : employees) {
            response.add(mapToResponse(employee));
        }
        return response;
    }

    // ── GET ACTIVE ONLY ───────────────────────────────────────────────
    public List<EmployeeResponseDTO> getActiveEmployees() {
        List<Employee> employees = employeeRepository.findByActive(true);
        List<EmployeeResponseDTO> response = new ArrayList<>();
        for (Employee employee : employees) {
            response.add(mapToResponse(employee));
        }
        return response;
    }

    // ── SEARCH ───────────────────────────────────────────────────────
    public List<EmployeeResponseDTO> searchEmployees(String keyword) {
        List<Employee> employees = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword);
        List<EmployeeResponseDTO> response = new ArrayList<>();
        for (Employee employee : employees) {
            response.add(mapToResponse(employee));
        }
        return response;
    }

    // ── REPORT: BY DEPARTMENT ─────────────────────────────────────────
    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId) {
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActive(departmentId, true);
        List<EmployeeResponseDTO> response = new ArrayList<>();
        for (Employee employee : employees) {
            response.add(mapToResponse(employee));
        }
        return response;
    }

    // ── REPORT: BY AGE ────────────────────────────────────────────────
    public List<EmployeeResponseDTO> getEmployeesOrderedByAge() {
        List<Employee> employees = employeeRepository.findByActiveOrderByDateOfBirthAsc(true);
        List<EmployeeResponseDTO> response = new ArrayList<>();
        for (Employee employee : employees) {
            response.add(mapToResponse(employee));
        }
        return response;
    }

    // ── PROCESSING: AVERAGE SALARY ────────────────────────────────────
    public double getAverageSalary() {
        List<Employee> employees = employeeRepository.findByActive(true);
        if (employees.isEmpty()) return 0.0;

        double total = 0;
        for (Employee employee : employees) {
            total += employee.getSalary().doubleValue();
        }
        return total / employees.size();
    }

    // ── PROCESSING: AVERAGE AGE ───────────────────────────────────────
    public double getAverageAge() {
        List<Employee> employees = employeeRepository.findByActive(true);
        if (employees.isEmpty()) return 0.0;

        double totalAge = 0;
        for (Employee employee : employees) {
            totalAge += employee.getAge();
        }
        return totalAge / employees.size();
    }

    // ── MAPPER: Entity → ResponseDTO ─────────────────────────────────
    private EmployeeResponseDTO mapToResponse(Employee employee) {
        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setId(employee.getId());
        response.setEmployeeId(employee.getEmployeeId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setDateOfBirth(employee.getDateOfBirth());  // age auto-calculated
        response.setDepartmentName(employee.getDepartment().getName());
        response.setSalary(employee.getSalary());
        response.setActive(employee.isActive());
        response.setCreatedAt(employee.getCreatedAt());
        return response;
    }
}