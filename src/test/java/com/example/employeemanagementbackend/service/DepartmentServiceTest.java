package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.DepartmentRequestDTO;
import com.example.employeemanagementbackend.dto.DepartmentResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department mockDepartment;
    private DepartmentRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockDepartment = new Department();
        mockDepartment.setId(1L);
        mockDepartment.setName("Engineering");
        mockDepartment.setActive(true);

        validRequest = new DepartmentRequestDTO("Engineering");
    }

    // ───── POSITIVE TESTS ─────

    @Test
    void addDepartment_Success() {
        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(mockDepartment);

        DepartmentResponseDTO response = departmentService.addDepartment(validRequest);

        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertTrue(response.isActive());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void getAllDepartments_Success() {
        when(departmentRepository.findByActive(true)).thenReturn(List.of(mockDepartment));

        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void getAllDepartments_EmptyList_ReturnsEmptyList() {
        when(departmentRepository.findByActive(true)).thenReturn(List.of());

        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));

        DepartmentResponseDTO response = departmentService.getDepartmentById(1L);

        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertTrue(response.isActive());
    }

    @Test
    void updateDepartment_Success() {
        DepartmentRequestDTO updateRequest = new DepartmentRequestDTO("HR");
        Department updatedDepartment = new Department();
        updatedDepartment.setId(1L);
        updatedDepartment.setName("HR");
        updatedDepartment.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.findByName("HR")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        DepartmentResponseDTO response = departmentService.updateDepartment(1L, updateRequest);

        assertNotNull(response);
        assertEquals("HR", response.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void deleteDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(false);

        departmentService.deleteDepartment(1L);

        assertFalse(mockDepartment.isActive());
        verify(departmentRepository, times(1)).save(mockDepartment);
    }

    @Test
    void searchByName_Success() {
        when(departmentRepository.findByNameContainingIgnoreCase("eng"))
                .thenReturn(List.of(mockDepartment));

        List<DepartmentResponseDTO> result = departmentService.searchByName("eng");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
    }

    @Test
    void searchByName_NoResults_ReturnsEmptyList() {
        when(departmentRepository.findByNameContainingIgnoreCase("xyz"))
                .thenReturn(List.of());

        List<DepartmentResponseDTO> result = departmentService.searchByName("xyz");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ───── NEGATIVE TESTS ─────

    @Test
    void addDepartment_NullName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.addDepartment(request));
        assertEquals("Department name is required.", ex.getMessage());
    }

    @Test
    void addDepartment_EmptyName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.addDepartment(request));
        assertEquals("Department name is required.", ex.getMessage());
    }

    @Test
    void addDepartment_DuplicateName_ThrowsDuplicateEntryException() {
        when(departmentRepository.findByName("Engineering"))
                .thenReturn(Optional.of(mockDepartment));

        DuplicateEntryException ex = assertThrows(DuplicateEntryException.class,
                () -> departmentService.addDepartment(validRequest));
        assertEquals("Department already exists: Engineering", ex.getMessage());
    }

    @Test
    void getDepartmentById_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(1L));
        assertEquals("Department not found with id: 1", ex.getMessage());
    }

    @Test
    void updateDepartment_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(1L, validRequest));
        assertEquals("Department not found with id: 1", ex.getMessage());
    }

    @Test
    void updateDepartment_NullName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO(null);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.updateDepartment(1L, request));
        assertEquals("Department name is required.", ex.getMessage());
    }

    @Test
    void updateDepartment_DuplicateName_ThrowsDuplicateEntryException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("HR");
        Department anotherDepartment = new Department();
        anotherDepartment.setId(2L);
        anotherDepartment.setName("HR");
        anotherDepartment.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.findByName("HR")).thenReturn(Optional.of(anotherDepartment));

        DuplicateEntryException ex = assertThrows(DuplicateEntryException.class,
                () -> departmentService.updateDepartment(1L, request));
        assertEquals("Department name already exists: HR", ex.getMessage());
    }

    @Test
    void deleteDepartment_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(1L));
        assertEquals("Department not found with id: 1", ex.getMessage());
    }

    @Test
    void deleteDepartment_HasActiveEmployees_ThrowsIllegalArgumentException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.deleteDepartment(1L));
        assertEquals("Cannot deactivate department because it has active employees. " +
                "Please reassign or deactivate all employees first.", ex.getMessage());
    }

    @Test
    void searchByName_EmptyName_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName(""));
        assertEquals("Department name search term is required.", ex.getMessage());
    }

    @Test
    void searchByName_NullName_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName(null));
        assertEquals("Department name search term is required.", ex.getMessage());
    }
}