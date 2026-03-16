package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.EmployeeRequestDTO;
import com.example.employeemanagementbackend.dto.EmployeeResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.entity.Employee;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.DepartmentRepository;
import com.example.employeemanagementbackend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private Department mockDepartment;
    private EmployeeRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockDepartment = new Department();
        mockDepartment.setId(1L);
        mockDepartment.setName("Engineering");

        mockEmployee = new Employee();
        mockEmployee.setFirstName("Jane");
        mockEmployee.setLastName("Smith");
        mockEmployee.setDateOfBirth(LocalDate.of(1995, 5, 15));
        mockEmployee.setDepartment(mockDepartment);
        mockEmployee.setSalary(new BigDecimal("50000"));
        mockEmployee.setActive(true);

        validRequest = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.of(1995, 5, 15),
                1L,
                new BigDecimal("50000")
        );
    }

    // ───── POSITIVE TESTS ─────

    @Test
    void create_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByEmployeeId(anyLong())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        EmployeeResponseDTO response = employeeService.create(validRequest);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("Engineering", response.getDepartmentName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getAll_Success() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    @Test
    void getAll_EmptyList_ReturnsEmptyList() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of());

        List<EmployeeResponseDTO> result = employeeService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        EmployeeResponseDTO response = employeeService.getById(1L);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void getByEmployeeId_Success() {
        when(employeeRepository.findByEmployeeId(100001L)).thenReturn(Optional.of(mockEmployee));

        EmployeeResponseDTO response = employeeService.getByEmployeeId(100001L);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void update_Success() {
        EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                "Janet", "Smith",
                LocalDate.of(1995, 5, 15),
                1L,
                new BigDecimal("60000")
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        EmployeeResponseDTO response = employeeService.update(1L, updateRequest);

        assertNotNull(response);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void delete_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        employeeService.delete(1L);

        assertFalse(mockEmployee.isActive());
        verify(employeeRepository, times(1)).save(mockEmployee);
    }

    @Test
    void getByDepartment_Success() {
        when(employeeRepository.findByDepartmentIdAndActive(1L, true))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.getByDepartment(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartmentName());
    }

    @Test
    void getAllOrderedByAge_Success() {
        when(employeeRepository.findByActiveOrderByDateOfBirthAsc(true))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.getAllOrderedByAge();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAverageSalary_Success() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of(mockEmployee));

        BigDecimal avg = employeeService.getAverageSalary();

        assertEquals(new BigDecimal("50000.00"), avg);
    }

    @Test
    void getAverageSalary_NoEmployees_ReturnsZero() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of());

        BigDecimal avg = employeeService.getAverageSalary();

        assertEquals(BigDecimal.ZERO, avg);
    }

    @Test
    void getAverageAge_Success() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of(mockEmployee));

        double avg = employeeService.getAverageAge();

        assertTrue(avg > 0);
    }

    @Test
    void getAverageAge_NoEmployees_ReturnsZero() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of());

        double avg = employeeService.getAverageAge();

        assertEquals(0.0, avg);
    }

    // ───── SEARCH TESTS ─────

    @Test
    void searchByName_Success() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                "Jane", "Jane")).thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchByName("Jane");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    @Test
    void searchByName_NoResults_ReturnsEmptyList() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                "xyz", "xyz")).thenReturn(List.of());

        List<EmployeeResponseDTO> result = employeeService.searchByName("xyz");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void searchByName_NullName_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchByName(null));
        assertEquals("Name search term is required.", ex.getMessage());
    }

    @Test
    void searchByName_EmptyName_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchByName(""));
        assertEquals("Name search term is required.", ex.getMessage());
    }

    // ───── FILTER TESTS ─────

    @Test
    void filterByDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentIdAndActive(1L, true))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.filterByDepartment(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartmentName());
    }

    @Test
    void filterByDepartment_NoResults_ReturnsEmptyList() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentIdAndActive(1L, true))
                .thenReturn(List.of());

        List<EmployeeResponseDTO> result = employeeService.filterByDepartment(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void filterByDepartment_NullDepartmentId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.filterByDepartment(null));
        assertEquals("Department ID is required.", ex.getMessage());
    }

    @Test
    void filterByDepartment_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.filterByDepartment(99L));
        assertEquals("Department not found with id: 99", ex.getMessage());
    }

    // ───── COMBINED SEARCH + FILTER TESTS ─────

    @Test
    void searchAndFilter_BothNull_ReturnsAllEmployees() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchAndFilter(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchAndFilter_NameOnly_Success() {
        when(employeeRepository.searchAndFilter("Jane", null))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchAndFilter("Jane", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    @Test
    void searchAndFilter_DepartmentOnly_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.searchAndFilter(null, 1L))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchAndFilter(null, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartmentName());
    }

    @Test
    void searchAndFilter_NameAndDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.searchAndFilter("Jane", 1L))
                .thenReturn(List.of(mockEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchAndFilter("Jane", 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
        assertEquals("Engineering", result.get(0).getDepartmentName());
    }

    @Test
    void searchAndFilter_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.searchAndFilter("Jane", 99L));
        assertEquals("Department not found with id: 99", ex.getMessage());
    }

    @Test
    void searchAndFilter_NoResults_ReturnsEmptyList() {
        when(employeeRepository.searchAndFilter("xyz", null))
                .thenReturn(List.of());

        List<EmployeeResponseDTO> result = employeeService.searchAndFilter("xyz", null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ───── NEGATIVE TESTS ─────

    @Test
    void create_NullFirstName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                null, "Smith",
                LocalDate.of(1995, 5, 15),
                1L, new BigDecimal("50000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void create_EmptyFirstName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "", "Smith",
                LocalDate.of(1995, 5, 15),
                1L, new BigDecimal("50000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void create_NullLastName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", null,
                LocalDate.of(1995, 5, 15),
                1L, new BigDecimal("50000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("Last name is required.", ex.getMessage());
    }

    @Test
    void create_NullDateOfBirth_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                null,
                1L, new BigDecimal("50000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("Date of birth is required.", ex.getMessage());
    }

    @Test
    void create_AgeTooYoung_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.now().minusYears(17),
                1L, new BigDecimal("50000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("Employee must be at least 18 years old.", ex.getMessage());
    }

    @Test
    void create_NullSalary_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.of(1995, 5, 15),
                1L, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("Salary must be greater than zero.", ex.getMessage());
    }

    @Test
    void create_InvalidSalary_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.of(1995, 5, 15),
                1L, new BigDecimal("-1000")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.create(request));
        assertEquals("Salary must be greater than zero.", ex.getMessage());
    }

    @Test
    void create_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.create(validRequest));
        assertEquals("Department not found.", ex.getMessage());
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getById(1L));
        assertEquals("Employee not found with id: 1", ex.getMessage());
    }

    @Test
    void getByEmployeeId_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findByEmployeeId(100001L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getByEmployeeId(100001L));
        assertEquals("Employee not found with employeeId: 100001", ex.getMessage());
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.update(1L, validRequest));
        assertEquals("Employee not found with id: 1", ex.getMessage());
    }

    @Test
    void update_AgeTooYoung_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.now().minusYears(17),
                1L, new BigDecimal("50000")
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> employeeService.update(1L, request));
        assertEquals("Employee must be at least 18 years old.", ex.getMessage());
    }

    @Test
    void update_DepartmentNotFound_ThrowsResourceNotFoundException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith",
                LocalDate.of(1995, 5, 15),
                99L, new BigDecimal("50000")
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.update(1L, request));
        assertEquals("Department not found.", ex.getMessage());
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.delete(1L));
        assertEquals("Employee not found with id: 1", ex.getMessage());
    }
}