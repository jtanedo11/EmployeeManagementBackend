package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.LoginRequestDTO;
import com.example.employeemanagementbackend.dto.LoginResponseDTO;
import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.UserRepository;
import com.example.employeemanagementbackend.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private LoginRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setUsername("johndoe");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(User.Role.USER);
        mockUser.setActive(true);

        validRequest = new LoginRequestDTO("johndoe", "password123");
    }

    // ───── POSITIVE TESTS ─────

    @Test
    void login_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("johndoe")).thenReturn("mockToken");

        LoginResponseDTO response = authService.login(validRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("Login successful", response.getMessage());
        verify(jwtTokenUtil, times(1)).generateToken("johndoe");
    }

    @Test
    void logout_Success() {
        String result = authService.logout();
        assertEquals("Logged out successfully", result);
    }

    // ───── NEGATIVE TESTS ─────

    @Test
    void login_NullUsername_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO(null, "password123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Username cannot be empty", ex.getMessage());
    }

    @Test
    void login_EmptyUsername_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("", "password123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Username cannot be empty", ex.getMessage());
    }

    @Test
    void login_NullPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Password cannot be empty.", ex.getMessage());
    }

    @Test
    void login_EmptyPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Password cannot be empty.", ex.getMessage());
    }

    @Test
    void login_ShortPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "short");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Password must be at least 8 characters.", ex.getMessage());
    }

    @Test
    void login_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> authService.login(validRequest));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void login_IncorrectPassword_ThrowsIllegalArgumentException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(validRequest));
        assertEquals("Incorrect password", ex.getMessage());
    }

    @Test
    void login_TokenNotGeneratedForWrongPassword() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(validRequest));

        // Verify token was never generated
        verify(jwtTokenUtil, never()).generateToken(anyString());
    }
}