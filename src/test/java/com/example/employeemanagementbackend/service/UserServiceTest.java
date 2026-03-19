package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.UserRequestDTO;
import com.example.employeemanagementbackend.dto.UserResponseDTO;
import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UserRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setUsername("johndoe");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(User.Role.USER);
        mockUser.setActive(true);

        validRequest = new UserRequestDTO("John", "Doe", "johndoe", "password123", "USER");
    }

    // ───── ADD USER ─────

    @Test
    void addUser_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponseDTO response = userService.addUser(validRequest);

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
        assertTrue(response.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void addUser_NullFirstName_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO(null, "Doe", "johndoe", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void addUser_EmptyFirstName_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("", "Doe", "johndoe", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void addUser_WhitespaceFirstName_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("   ", "Doe", "johndoe", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void addUser_NullLastName_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", null, "johndoe", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Last name is required.", ex.getMessage());
    }

    @Test
    void addUser_EmptyLastName_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", "", "johndoe", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Last name is required.", ex.getMessage());
    }

    @Test
    void addUser_NullUsername_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", "Doe", null, "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void addUser_EmptyUsername_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", "Doe", "", "password123", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void addUser_NullPassword_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", "Doe", "johndoe", null, "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    void addUser_ShortPassword_ThrowsIllegalArgumentException() {
        UserRequestDTO request = new UserRequestDTO("John", "Doe", "johndoe", "short", "USER");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        assertEquals("Password must be at least 8 characters.", ex.getMessage());
    }

    @Test
    void addUser_ExactlyEightCharPassword_Success() {
        UserRequestDTO request = new UserRequestDTO("John", "Doe", "johndoe", "pass1234", "USER");
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass1234")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponseDTO response = userService.addUser(request);
        assertNotNull(response);
    }

    @Test
    void addUser_DuplicateUsername_ThrowsDuplicateEntryException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        DuplicateEntryException ex = assertThrows(DuplicateEntryException.class, () -> userService.addUser(validRequest));
        assertEquals("Username already exists: johndoe", ex.getMessage());
    }

    // ───── GET ALL ─────

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.getAllUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
    }

    @Test
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserResponseDTO> result = userService.getAllUsers();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ───── GET BY ID ─────

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        UserResponseDTO response = userService.getUserById(1L);
        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        assertEquals("User not found with id: 1", ex.getMessage());
    }

    // ───── UPDATE ─────

    @Test
    void updateUser_Success() {
        UserRequestDTO updateRequest = new UserRequestDTO("Jane", "Doe", "janedoe", "newpassword123", "ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("janedoe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponseDTO response = userService.updateUser(1L, updateRequest);
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, validRequest));
        assertEquals("User not found with id: 1", ex.getMessage());
    }

    @Test
    void updateUser_DuplicateUsername_ThrowsDuplicateEntryException() {
        UserRequestDTO updateRequest = new UserRequestDTO("John", "Doe", "existinguser", "password123", "USER");
        User anotherUser = new User();
        anotherUser.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(anotherUser));

        DuplicateEntryException ex = assertThrows(DuplicateEntryException.class, () -> userService.updateUser(1L, updateRequest));
        assertEquals("Username already exists: existinguser", ex.getMessage());
    }

    @Test
    void updateUser_ShortPassword_ThrowsIllegalArgumentException() {
        UserRequestDTO updateRequest = new UserRequestDTO("John", "Doe", "johndoe", "short", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updateRequest));
        assertEquals("Password must be at least 8 characters.", ex.getMessage());
    }

    @Test
    void updateUser_SevenCharPassword_ThrowsIllegalArgumentException() {
        UserRequestDTO updateRequest = new UserRequestDTO("John", "Doe", "johndoe", "1234567", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updateRequest));
        assertEquals("Password must be at least 8 characters.", ex.getMessage());
    }

    // ───── DELETE ─────

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        userService.deleteUser(1L);
        assertFalse(mockUser.isActive());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void deleteUser_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        assertEquals("User not found with id: 1", ex.getMessage());
    }

    // ───── SEARCH ─────

    @Test
    void searchByUsername_Success() {
        when(userRepository.findByUsernameContainingIgnoreCase("john")).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.searchByUsername("john");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
    }

    @Test
    void searchByUsername_NoResults_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsernameContainingIgnoreCase("xyz")).thenReturn(List.of());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.searchByUsername("xyz"));
        assertEquals("No users found with username: xyz", ex.getMessage());
    }

    @Test
    void searchByUsername_NullUsername_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.searchByUsername(null));
        assertEquals("Username search term is required.", ex.getMessage());
    }

    @Test
    void searchByUsername_EmptyUsername_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.searchByUsername(""));
        assertEquals("Username search term is required.", ex.getMessage());
    }

    // ───── FILTER BY ROLE ─────

    @Test
    void filterByRole_Admin_Success() {
        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setActive(true);

        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of(adminUser));
        List<UserResponseDTO> result = userService.filterByRole("ADMIN");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void filterByRole_User_Success() {
        when(userRepository.findByRole(User.Role.USER)).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.filterByRole("USER");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
    }

    @Test
    void filterByRole_NoResults_ReturnsEmptyList() {
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of());
        List<UserResponseDTO> result = userService.filterByRole("ADMIN");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void filterByRole_InvalidRole_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.filterByRole("SUPERADMIN"));
        assertEquals("Invalid role: SUPERADMIN. Must be ADMIN or USER.", ex.getMessage());
    }

    @Test
    void filterByRole_NullRole_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.filterByRole(null));
        assertEquals("Role is required.", ex.getMessage());
    }

    @Test
    void filterByRole_EmptyRole_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.filterByRole(""));
        assertEquals("Role is required.", ex.getMessage());
    }

    // ───── COMBINED SEARCH + FILTER ─────

    @Test
    void searchAndFilter_BothNull_ReturnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.searchAndFilter(null, null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchAndFilter_UsernameOnly_Success() {
        when(userRepository.searchAndFilter("john", null)).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.searchAndFilter("john", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
    }

    @Test
    void searchAndFilter_RoleOnly_Success() {
        when(userRepository.searchAndFilter(null, User.Role.USER)).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.searchAndFilter(null, "USER");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchAndFilter_UsernameAndRole_Success() {
        when(userRepository.searchAndFilter("john", User.Role.USER)).thenReturn(List.of(mockUser));
        List<UserResponseDTO> result = userService.searchAndFilter("john", "USER");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getUsername());
    }

    @Test
    void searchAndFilter_InvalidRole_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.searchAndFilter("john", "SUPERADMIN"));
        assertEquals("Invalid role: SUPERADMIN. Must be ADMIN or USER.", ex.getMessage());
    }

    @Test
    void searchAndFilter_NoResults_ReturnsEmptyList() {
        when(userRepository.searchAndFilter("xyz", null)).thenReturn(List.of());
        List<UserResponseDTO> result = userService.searchAndFilter("xyz", null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}