package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User mockActiveUser;
    private User mockInactiveUser;

    @BeforeEach
    void setUp() {
        mockActiveUser = new User();
        mockActiveUser.setFirstName("John");
        mockActiveUser.setLastName("Doe");
        mockActiveUser.setUsername("johndoe");
        mockActiveUser.setPassword("encodedPassword");
        mockActiveUser.setRole(User.Role.USER);
        mockActiveUser.setActive(true);

        mockInactiveUser = new User();
        mockInactiveUser.setFirstName("Jane");
        mockInactiveUser.setLastName("Doe");
        mockInactiveUser.setUsername("janedoe");
        mockInactiveUser.setPassword("encodedPassword");
        mockInactiveUser.setRole(User.Role.USER);
        mockInactiveUser.setActive(false);
    }

    // ───── POSITIVE TESTS ─────

    @Test
    void loadUserByUsername_ActiveUser_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockActiveUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("johndoe");

        assertNotNull(userDetails);
        assertEquals("johndoe", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_AdminUser_Success() {
        mockActiveUser.setRole(User.Role.ADMIN);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(mockActiveUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("johndoe");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // ───── NEGATIVE TESTS ─────

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nobody"));
        assertEquals("User not found: nobody", ex.getMessage());
    }

    @Test
    void loadUserByUsername_DeactivatedUser_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername("janedoe")).thenReturn(Optional.of(mockInactiveUser));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("janedoe"));
        assertEquals("Account is deactivated.", ex.getMessage());
    }
}