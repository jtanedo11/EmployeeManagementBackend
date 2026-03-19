package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.DepartmentRequestDTO;
import com.example.employeemanagementbackend.dto.DepartmentResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import com.example.employeemanagementbackend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // ADD
    public DepartmentResponseDTO addDepartment(DepartmentRequestDTO request) {

        ValidationUtil.validateDepartmentName(request.getName());

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException("Department already exists: " + request.getName());
        }

        Department department = new Department();
        department.setName(request.getName());

        return mapToResponse(departmentRepository.save(department));
    }

    // GET ALL — only return active departments
    public List<DepartmentResponseDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentResponseDTO> result = new ArrayList<>();
        for (Department dept : departments) {
            result.add(mapToResponse(dept));
        }
        return result;
    }

    // GET BY ID
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToResponse(department);
    }

    // SEARCH BY NAME
    public List<DepartmentResponseDTO> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name search term is required.");
        }
        List<Department> departments = departmentRepository.findByNameContainingIgnoreCase(name);

        if (departments.isEmpty()) {
            throw new ResourceNotFoundException("No departments found with name: " + name);
        }

        List<DepartmentResponseDTO> result = new ArrayList<>();
        for (Department dept : departments) {
            result.add(mapToResponse(dept));
        }
        return result;
    }

    // UPDATE
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        ValidationUtil.validateDepartmentName(request.getName());

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException("Department name already exists: " + request.getName());
        }

        department.setName(request.getName());
        return mapToResponse(departmentRepository.save(department));
    }

    // REACTIVATE
    public DepartmentResponseDTO activateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        department.setActive(true);
        return mapToResponse(departmentRepository.save(department));
    }

    // DELETE — soft delete with employee check
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (employeeRepository.existsByDepartmentIdAndActive(id, true)) {
            throw new IllegalArgumentException(
                    "Cannot deactivate department because it has active employees. " +
                            "Please reassign or deactivate all employees first.");
        }

        department.setActive(false);
        departmentRepository.save(department);
    }

    // MAPPER
    private DepartmentResponseDTO mapToResponse(Department department) {
        DepartmentResponseDTO response = new DepartmentResponseDTO();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setActive(department.isActive());
        return response;
    }
}