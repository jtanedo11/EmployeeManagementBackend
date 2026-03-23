package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.LoginRequestDTO;
import com.example.employeemanagementbackend.dto.LoginResponseDTO;
import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.exception.AccountDeactivatedException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.UserRepository;
import com.example.employeemanagementbackend.security.JwtTokenUtil;
import com.example.employeemanagementbackend.util.MessageUtil;
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

    @Mock
    private MessageUtil messageUtil;

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

        lenient().when(messageUtil.get(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(messageUtil.get(anyString(), any())).thenAnswer(i -> i.getArgument(0));
    }

    // ───── LOGIN POSITIVE ─────

    @Test
    void login_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("johndoe")).thenReturn("mockToken");

        LoginResponseDTO response = authService.login(validRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(jwtTokenUtil, times(1)).generateToken("johndoe");
    }

    @Test
    void login_Success_ReturnsRole() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("johndoe")).thenReturn("mockToken");

        LoginResponseDTO response = authService.login(validRequest);

        assertNotNull(response.getRole());
        assertEquals("USER", response.getRole());
    }

    @Test
    void login_AdminUser_Success() {
        mockUser.setRole(User.Role.ADMIN);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("johndoe")).thenReturn("mockToken");

        LoginResponseDTO response = authService.login(validRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    // ───── LOGIN NEGATIVE ─────

    @Test
    void login_NullUsername_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO(null, "password123");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_EmptyUsername_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("", "password123");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_WhitespaceUsername_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("   ", "password123");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_NullPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", null);
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_EmptyPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_WhitespacePassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "   ");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_ShortPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "short");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_SevenCharPassword_ThrowsIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO("johndoe", "1234567");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.login(validRequest));
    }

    @Test
    void login_IncorrectPassword_ThrowsIllegalArgumentException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.login(validRequest));
    }

    @Test
    void login_IncorrectPassword_TokenNeverGenerated() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.login(validRequest));
        verify(jwtTokenUtil, never()).generateToken(anyString());
    }

    @Test
    void login_UserNotFound_TokenNeverGenerated() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.login(validRequest));
        verify(jwtTokenUtil, never()).generateToken(anyString());
    }

    // ───── DEACTIVATED ACCOUNT ─────

    @Test
    void login_DeactivatedAccount_ThrowsAccountDeactivatedException() {
        mockUser.setActive(false);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        assertThrows(AccountDeactivatedException.class, () -> authService.login(validRequest));
    }

    @Test
    void login_DeactivatedAccount_TokenNeverGenerated() {
        mockUser.setActive(false);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        assertThrows(AccountDeactivatedException.class, () -> authService.login(validRequest));
        verify(jwtTokenUtil, never()).generateToken(anyString());
    }

    @Test
    void login_DeactivatedAccount_WrongPassword_ThrowsIllegalArgumentException() {
        mockUser.setActive(false);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.login(validRequest));
    }

    // ───── LOGOUT ─────

    @Test
    void logout_Success() {
        String result = authService.logout();
        assertNotNull(result);
    }

    @Test
    void logout_DoesNotCallRepository() {
        authService.logout();
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtTokenUtil);
    }
}