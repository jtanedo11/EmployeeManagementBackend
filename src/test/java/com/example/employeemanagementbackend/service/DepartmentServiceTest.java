package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.DepartmentRequestDTO;
import com.example.employeemanagementbackend.dto.DepartmentResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import com.example.employeemanagementbackend.util.MessageUtil;
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

    @Mock
    private MessageUtil messageUtil;

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

        lenient().when(messageUtil.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messageUtil.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    // ───── ADD ─────

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
    void addDepartment_NullName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO(null);
        assertThrows(IllegalArgumentException.class, () -> departmentService.addDepartment(request));
    }

    @Test
    void addDepartment_EmptyName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("");
        assertThrows(IllegalArgumentException.class, () -> departmentService.addDepartment(request));
    }

    @Test
    void addDepartment_WhitespaceName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("   ");
        assertThrows(IllegalArgumentException.class, () -> departmentService.addDepartment(request));
    }

    @Test
    void addDepartment_DuplicateName_ThrowsDuplicateEntryException() {
        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.of(mockDepartment));
        assertThrows(DuplicateEntryException.class, () -> departmentService.addDepartment(validRequest));
    }

    @Test
    void addDepartment_RepositoryCalledOnce_OnSuccess() {
        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(mockDepartment);

        departmentService.addDepartment(validRequest);

        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    // ───── GET ALL ─────

    @Test
    void getAllDepartments_Success() {
        when(departmentRepository.findAll()).thenReturn(List.of(mockDepartment));
        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
    }

    @Test
    void getAllDepartments_IncludesInactive() {
        Department inactiveDept = new Department();
        inactiveDept.setId(2L);
        inactiveDept.setName("Old Dept");
        inactiveDept.setActive(false);

        when(departmentRepository.findAll()).thenReturn(List.of(mockDepartment, inactiveDept));
        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllDepartments_EmptyList_ReturnsEmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());
        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ───── GET BY ID ─────

    @Test
    void getDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        DepartmentResponseDTO response = departmentService.getDepartmentById(1L);
        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertTrue(response.isActive());
    }

    @Test
    void getDepartmentById_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(1L));
    }

    // ───── UPDATE ─────

    @Test
    void updateDepartment_Success() {
        DepartmentRequestDTO updateRequest = new DepartmentRequestDTO("HR");
        Department updatedDept = new Department();
        updatedDept.setId(1L);
        updatedDept.setName("HR");
        updatedDept.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.findByName("HR")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDept);

        DepartmentResponseDTO response = departmentService.updateDepartment(1L, updateRequest);
        assertNotNull(response);
        assertEquals("HR", response.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(1L, validRequest));
    }

    @Test
    void updateDepartment_NullName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO(null);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.updateDepartment(1L, request));
    }

    @Test
    void updateDepartment_EmptyName_ThrowsIllegalArgumentException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.updateDepartment(1L, request));
    }

    @Test
    void updateDepartment_DuplicateName_ThrowsDuplicateEntryException() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("HR");
        Department anotherDept = new Department();
        anotherDept.setId(2L);
        anotherDept.setName("HR");
        anotherDept.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.findByName("HR")).thenReturn(Optional.of(anotherDept));

        assertThrows(DuplicateEntryException.class,
                () -> departmentService.updateDepartment(1L, request));
    }

    // ───── ACTIVATE ─────

    @Test
    void activateDepartment_Success() {
        mockDepartment.setActive(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.save(mockDepartment)).thenReturn(mockDepartment);

        DepartmentResponseDTO response = departmentService.activateDepartment(1L);

        assertNotNull(response);
        assertTrue(mockDepartment.isActive());
        verify(departmentRepository, times(1)).save(mockDepartment);
    }

    @Test
    void activateDepartment_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.activateDepartment(99L));
    }

    @Test
    void activateDepartment_AlreadyActive_StillSaves() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(departmentRepository.save(mockDepartment)).thenReturn(mockDepartment);

        departmentService.activateDepartment(1L);

        assertTrue(mockDepartment.isActive());
        verify(departmentRepository, times(1)).save(mockDepartment);
    }

    // ───── DELETE ─────

    @Test
    void deleteDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(false);

        departmentService.deleteDepartment(1L);

        assertFalse(mockDepartment.isActive());
        verify(departmentRepository, times(1)).save(mockDepartment);
    }

    @Test
    void deleteDepartment_NotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(1L));
    }

    @Test
    void deleteDepartment_HasActiveEmployees_ThrowsIllegalArgumentException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.deleteDepartment(1L));
    }

    @Test
    void deleteDepartment_AlreadyInactive_SetsFalseAndSaves() {
        mockDepartment.setActive(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(false);

        departmentService.deleteDepartment(1L);

        assertFalse(mockDepartment.isActive());
        verify(departmentRepository, times(1)).save(mockDepartment);
    }

    // ───── SEARCH ─────

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
    void searchByName_NoResults_ThrowsResourceNotFoundException() {
        when(departmentRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.searchByName("xyz"));
    }

    @Test
    void searchByName_NullName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName(null));
    }

    @Test
    void searchByName_EmptyName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName(""));
    }

    @Test
    void searchByName_WhitespaceName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName("   "));
    }
}