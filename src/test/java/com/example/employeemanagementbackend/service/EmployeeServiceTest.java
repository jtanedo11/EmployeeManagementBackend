package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.EmployeeRequestDTO;
import com.example.employeemanagementbackend.dto.EmployeeResponseDTO;
import com.example.employeemanagementbackend.entity.Department;
import com.example.employeemanagementbackend.entity.Employee;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

    @Mock
    private MessageUtil messageUtil;

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
        mockDepartment.setActive(true);

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

        // Default stub for all messageUtil.get calls
        lenient().when(messageUtil.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messageUtil.get(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    // ───── CREATE ─────

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
    void create_NullFirstName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                null, "Smith", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_EmptyFirstName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "", "Smith", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_NullLastName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", null, LocalDate.of(1995, 5, 15), 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_EmptyLastName_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_NullDateOfBirth_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", null, 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_AgeTooYoung_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.now().minusYears(17), 1L, new BigDecimal("50000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_NullSalary_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 1L, null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_ZeroSalary_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 1L, BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_NegativeSalary_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("-1000"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(request));
    }

    @Test
    void create_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.create(validRequest));
    }

    // ───── GET ALL ─────

    @Test
    void getAll_Success() {
        when(employeeRepository.findAll()).thenReturn(List.of(mockEmployee));
        List<EmployeeResponseDTO> result = employeeService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    @Test
    void getAll_EmptyList_ReturnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        List<EmployeeResponseDTO> result = employeeService.getAll();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ───── GET BY ID ─────

    @Test
    void getById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        EmployeeResponseDTO response = employeeService.getById(1L);
        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(1L));
    }

    // ───── GET BY EMPLOYEE ID ─────

    @Test
    void getByEmployeeId_Success() {
        when(employeeRepository.findByEmployeeId(100001L)).thenReturn(Optional.of(mockEmployee));
        EmployeeResponseDTO response = employeeService.getByEmployeeId(100001L);
        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void getByEmployeeId_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findByEmployeeId(100001L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getByEmployeeId(100001L));
    }

    // ───── UPDATE ─────

    @Test
    void update_Success() {
        EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                "Janet", "Smith", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("60000"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        EmployeeResponseDTO response = employeeService.update(1L, updateRequest);
        assertNotNull(response);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(1L, validRequest));
    }

    @Test
    void update_AgeTooYoung_ThrowsIllegalArgumentException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.now().minusYears(17), 1L, new BigDecimal("50000"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, request));
    }

    @Test
    void update_ZeroSalary_DoesNotUpdateSalary() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 1L, BigDecimal.ZERO);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        employeeService.update(1L, request);
        assertEquals(new BigDecimal("50000"), mockEmployee.getSalary());
    }

    @Test
    void update_NegativeSalary_DoesNotUpdateSalary() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 1L, new BigDecimal("-500"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        employeeService.update(1L, request);
        assertEquals(new BigDecimal("50000"), mockEmployee.getSalary());
    }

    @Test
    void update_DepartmentNotFound_ThrowsResourceNotFoundException() {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Jane", "Smith", LocalDate.of(1995, 5, 15), 99L, new BigDecimal("50000"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(1L, request));
    }

    // ───── DELETE (DEACTIVATE) ─────

    @Test
    void delete_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        employeeService.delete(1L);
        assertFalse(mockEmployee.isActive());
        verify(employeeRepository, times(1)).save(mockEmployee);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(1L));
    }

    // ───── ACTIVATE ─────

    @Test
    void activate_Success() {
        mockEmployee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(employeeRepository.save(mockEmployee)).thenReturn(mockEmployee);

        EmployeeResponseDTO response = employeeService.activate(1L);
        assertNotNull(response);
        assertTrue(mockEmployee.isActive());
        verify(employeeRepository, times(1)).save(mockEmployee);
    }

    @Test
    void activate_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.activate(1L));
    }

    @Test
    void activate_InactiveDepartment_ThrowsIllegalArgumentException() {
        mockEmployee.setActive(false);
        mockDepartment.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.activate(1L));
    }

    // ───── SEARCH BY NAME ─────

    @Test
    void searchByName_Success() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Jane", "Jane"))
                .thenReturn(List.of(mockEmployee));
        List<EmployeeResponseDTO> result = employeeService.searchByName("Jane");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    @Test
    void searchByName_NoResults_ThrowsResourceNotFoundException() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("xyz", "xyz"))
                .thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.searchByName("xyz"));
    }

    @Test
    void searchByName_NullName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.searchByName(null));
    }

    @Test
    void searchByName_EmptyName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.searchByName(""));
    }

    // ───── FILTER BY DEPARTMENT ─────

    @Test
    void filterByDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentIdAndActive(1L, true)).thenReturn(List.of(mockEmployee));
        List<EmployeeResponseDTO> result = employeeService.filterByDepartment(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartmentName());
    }

    @Test
    void filterByDepartment_NoResults_ReturnsEmptyList() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentIdAndActive(1L, true)).thenReturn(List.of());
        List<EmployeeResponseDTO> result = employeeService.filterByDepartment(1L);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void filterByDepartment_NullId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.filterByDepartment(null));
    }

    @Test
    void filterByDepartment_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.filterByDepartment(99L));
    }

    // ───── SEARCH AND FILTER ─────

    @Test
    void searchAndFilter_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(employeeRepository.searchAndFilterPageable(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.searchAndFilter(
                "Jane", 1L, true, null, null, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchAndFilter_EmptyName_TreatedAsNull() {
        Page<Employee> mockPage = new PageImpl<>(List.of());
        when(employeeRepository.searchAndFilterPageable(isNull(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.searchAndFilter(
                "", null, null, null, null, 0, 10);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchAndFilter_WithAgeRange_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(employeeRepository.searchAndFilterPageable(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.searchAndFilter(
                null, null, null, 25, 35, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ───── CALCULATIONS ─────

    @Test
    void getAverageSalary_NoFilter_Success() {
        when(employeeRepository.findForStats(isNull(), isNull(), isNull()))
                .thenReturn(List.of(mockEmployee));
        BigDecimal avg = employeeService.getAverageSalary(null, null, null);
        assertEquals(new BigDecimal("50000.00"), avg);
    }

    @Test
    void getAverageSalary_WithDepartmentFilter_Success() {
        when(employeeRepository.findForStats(eq(1L), isNull(), isNull()))
                .thenReturn(List.of(mockEmployee));
        BigDecimal avg = employeeService.getAverageSalary(1L, null, null);
        assertEquals(new BigDecimal("50000.00"), avg);
    }

    @Test
    void getAverageSalary_WithAgeFilter_Success() {
        when(employeeRepository.findForStats(isNull(), any(), any()))
                .thenReturn(List.of(mockEmployee));
        BigDecimal avg = employeeService.getAverageSalary(null, 25, 35);
        assertEquals(new BigDecimal("50000.00"), avg);
    }

    @Test
    void getAverageSalary_NoEmployees_ReturnsZero() {
        when(employeeRepository.findForStats(isNull(), isNull(), isNull()))
                .thenReturn(List.of());
        BigDecimal avg = employeeService.getAverageSalary(null, null, null);
        assertEquals(BigDecimal.ZERO, avg);
    }

    @Test
    void getAverageAge_NoFilter_Success() {
        when(employeeRepository.findForStats(isNull(), isNull(), isNull()))
                .thenReturn(List.of(mockEmployee));
        double avg = employeeService.getAverageAge(null, null, null);
        assertTrue(avg > 0);
    }

    @Test
    void getAverageAge_WithDepartmentFilter_Success() {
        when(employeeRepository.findForStats(eq(1L), isNull(), isNull()))
                .thenReturn(List.of(mockEmployee));
        double avg = employeeService.getAverageAge(1L, null, null);
        assertTrue(avg > 0);
    }

    @Test
    void getAverageAge_WithAgeFilter_Success() {
        when(employeeRepository.findForStats(isNull(), any(), any()))
                .thenReturn(List.of(mockEmployee));
        double avg = employeeService.getAverageAge(null, 25, 35);
        assertTrue(avg > 0);
    }

    @Test
    void getAverageAge_NoEmployees_ReturnsZero() {
        when(employeeRepository.findForStats(isNull(), isNull(), isNull()))
                .thenReturn(List.of());
        double avg = employeeService.getAverageAge(null, null, null);
        assertEquals(0.0, avg);
    }

    // ───── PAGINATED REPORTS ─────

    @Test
    void getByDepartmentPaged_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentIdAndActivePaged(eq(1L), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.getByDepartmentPaged(1L, true, 0, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByDepartmentPaged_DepartmentNotFound_ThrowsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getByDepartmentPaged(99L, null, 0, 5));
    }

    @Test
    void getAllOrderedByAgePaged_YoungsetFirst_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(employeeRepository.findAllActiveOrderByDateOfBirthAscPaged(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.getAllOrderedByAgePaged(
                true, null, null, "asc", 0, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllOrderedByAgePaged_OldestFirst_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(employeeRepository.findAllActiveOrderByDateOfBirthAscPaged(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.getAllOrderedByAgePaged(
                true, null, null, "desc", 0, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllOrderedByAgePaged_WithAgeFilter_Success() {
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));
        when(employeeRepository.findAllActiveOrderByDateOfBirthAscPaged(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.getAllOrderedByAgePaged(
                null, 25, 35, "asc", 0, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllOrderedByAgePaged_Empty_ReturnsEmptyPage() {
        Page<Employee> mockPage = new PageImpl<>(List.of());
        when(employeeRepository.findAllActiveOrderByDateOfBirthAscPaged(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<EmployeeResponseDTO> result = employeeService.getAllOrderedByAgePaged(
                null, null, null, "asc", 0, 5);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}