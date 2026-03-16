package com.example.employeemanagementbackend.controller;

import com.example.employeemanagementbackend.dto.EmployeeRequestDTO;
import com.example.employeemanagementbackend.dto.EmployeeResponseDTO;
import com.example.employeemanagementbackend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ADD
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // GET BY EMPLOYEE ID
    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getByEmployeeId(employeeId));
    }

    // SEARCH BY NAME
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchByName(name));
    }

    // COMBINED SEARCH + FILTER
    @GetMapping("/search-filter")
    public ResponseEntity<List<EmployeeResponseDTO>> searchAndFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(employeeService.searchAndFilter(name, departmentId));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
                                                              @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok("Employee deleted successfully.");
    }

    // ───── Reports ─────

    @GetMapping("/report/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.getByDepartment(departmentId));
    }

    @GetMapping("/report/age")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllOrderedByAge() {
        return ResponseEntity.ok(employeeService.getAllOrderedByAge());
    }

    // ───── Calculations ─────

    @GetMapping("/stats/average-salary")
    public ResponseEntity<BigDecimal> getAverageSalary() {
        return ResponseEntity.ok(employeeService.getAverageSalary());
    }

    @GetMapping("/stats/average-age")
    public ResponseEntity<Double> getAverageAge() {
        return ResponseEntity.ok(employeeService.getAverageAge());
    }
}