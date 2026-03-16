package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.EmployeeRequestDTO;
import com.example.employeemanagementbackend.dto.EmployeeResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.entity.Employee;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    // ───── CRUD ─────

    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (dto.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }

        // Age validation — must be at least 18
        int age = calculateAge(dto.getDateOfBirth());
        if (age < 18) {
            throw new IllegalArgumentException("Employee must be at least 18 years old.");
        }

        if (dto.getSalary() == null || dto.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salary must be greater than zero.");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));

        Employee employee = new Employee();
        employee.setEmployeeId(generateEmployeeId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setDepartment(department);
        employee.setSalary(dto.getSalary());
        employee.setActive(true);

        return mapToResponseDTO(employeeRepository.save(employee));
    }

    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponseDTO(employee);
    }

    public List<EmployeeResponseDTO> getAll() {
        List<Employee> employees = employeeRepository.findByActive(true);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) {
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) {
            employee.setLastName(dto.getLastName());
        }
        if (dto.getDateOfBirth() != null) {
            int age = calculateAge(dto.getDateOfBirth());
            if (age < 18) throw new IllegalArgumentException("Employee must be at least 18 years old.");
            employee.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getSalary() != null && dto.getSalary().compareTo(BigDecimal.ZERO) > 0) {
            employee.setSalary(dto.getSalary());
        }
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
            employee.setDepartment(department);
        }

        return mapToResponseDTO(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    // ───── Search ─────

    public EmployeeResponseDTO getByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with employeeId: " + employeeId));
        return mapToResponseDTO(employee);
    }

    // ADD THIS
    public List<EmployeeResponseDTO> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name search term is required.");
        }
        List<Employee> employees = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }
    // ───── Combined Search + Filter ─────

    public List<EmployeeResponseDTO> searchAndFilter(String name, Long departmentId) {

        // If department is provided, verify it exists
        if (departmentId != null) {
            departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + departmentId));
        }

        // Both null — just return all active employees
        if ((name == null || name.trim().isEmpty()) && departmentId == null) {
            return getAll();
        }

        // Clean up name — set to null if empty so query handles it properly
        String cleanName = (name == null || name.trim().isEmpty()) ? null : name.trim();

        List<Employee> employees = employeeRepository.searchAndFilter(cleanName, departmentId);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    // ───── Filter ─────

    public List<EmployeeResponseDTO> filterByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Department ID is required.");
        }
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActive(departmentId, true);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    // ───── Reports ─────

    public List<EmployeeResponseDTO> getByDepartment(Long departmentId) {
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActive(departmentId, true);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    public List<EmployeeResponseDTO> getAllOrderedByAge() {
        List<Employee> employees = employeeRepository.findByActiveOrderByDateOfBirthAsc(true);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    // ───── Calculations ─────

    public BigDecimal getAverageSalary() {
        List<Employee> employees = employeeRepository.findByActive(true);
        if (employees.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = BigDecimal.ZERO;
        for (Employee emp : employees) {
            total = total.add(emp.getSalary());
        }
        return total.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
    }

    public double getAverageAge() {
        List<Employee> employees = employeeRepository.findByActive(true);
        if (employees.isEmpty()) return 0.0;

        int totalAge = 0;
        for (Employee emp : employees) {
            totalAge += emp.getAge();
        }
        return (double) totalAge / employees.size();
    }

    // ───── Helpers ─────

    private int calculateAge(LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private Long generateEmployeeId() {
        Long employeeId;
        do {
            employeeId = (long) (Math.random() * 900000) + 100000;
        } while (employeeRepository.findByEmployeeId(employeeId).isPresent());
        return employeeId;
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setAge(employee.getAge());
        dto.setDepartmentName(employee.getDepartment() != null
                ? employee.getDepartment().getName() : "Unassigned");
        dto.setSalary(employee.getSalary());
        dto.setActive(employee.isActive());
        return dto;
    }
}