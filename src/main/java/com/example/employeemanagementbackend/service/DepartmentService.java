package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.DepartmentRequestDTO;
import com.example.employeemanagementbackend.dto.DepartmentResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // ADD
    public DepartmentResponseDTO addDepartment(DepartmentRequestDTO request) {

        // Check if department name already exists
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException("Department already exists: " + request.getName());
        }

        Department department = new Department();
        department.setName(request.getName());

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    // GET ALL
    public List<DepartmentResponseDTO> getAllDepartments() {

        // Collections requirement — using ArrayList explicitly
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentResponseDTO> response = new ArrayList<>();

        for (Department department : departments) {
            response.add(mapToResponse(department));
        }

        return response;
    }

    // GET BY ID
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        return mapToResponse(department);
    }

    // UPDATE
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if new name is already taken by another department
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException("Department name already exists: " + request.getName());
        }

        department.setName(request.getName());
        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    // DELETE
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        departmentRepository.delete(department);
    }

    // MAPPER
    // Converts Entity → ResponseDTO
    private DepartmentResponseDTO mapToResponse(Department department) {
        DepartmentResponseDTO response = new DepartmentResponseDTO();
        response.setId(department.getId());
        response.setName(department.getName());
        return response;
    }
}