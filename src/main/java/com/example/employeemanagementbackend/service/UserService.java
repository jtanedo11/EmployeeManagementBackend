package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.UserRequestDTO;
import com.example.employeemanagementbackend.dto.UserResponseDTO;
import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.exception.DuplicateEntryException;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.UserRepository;
import com.example.employeemanagementbackend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ADD
    public UserResponseDTO addUser(UserRequestDTO request) {

        ValidationUtil.validateName(request.getFirstName(), "First name");

        ValidationUtil.validateName(request.getLastName(), "Last name");

        ValidationUtil.validateUsername(request.getUsername());

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateEntryException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null
                ? User.Role.valueOf(request.getRole().toUpperCase())
                : User.Role.USER);

        return mapToResponseDTO(userRepository.save(user));
    }

    // GET ALL
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> result = new ArrayList<>();
        for (User user : users) {
            result.add(mapToResponseDTO(user));
        }
        return result;
    }

    // GET BY ID
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponseDTO(user);
    }

    // SEARCH BY USERNAME
    public List<UserResponseDTO> searchByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username search term is required.");
        }

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found with username: " + username);
        }

        List<UserResponseDTO> result = new ArrayList<>();
        for (User user : users) {
            result.add(mapToResponseDTO(user));
        }
        return result;
    }

    // FILTER BY ROLE
    public List<UserResponseDTO> filterByRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            List<UserResponseDTO> result = new ArrayList<>();
            for (User user : users) {
                result.add(mapToResponseDTO(user));
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Must be ADMIN or USER.");
        }
    }

    // Combined Search by name + Filter by role

    public List<UserResponseDTO> searchAndFilter(String username, String role) {

        // Convert role string to enum if provided
        User.Role userRole = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                userRole = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role + ". Must be ADMIN or USER.");
            }
        }

        // Both null — just return all users
        if ((username == null || username.trim().isEmpty()) && userRole == null) {
            return getAllUsers();
        }

        // Clean up username — set to null if empty
        String cleanUsername = (username == null || username.trim().isEmpty()) ? null : username.trim();

        List<User> users = userRepository.searchAndFilter(cleanUsername, userRole);
        List<UserResponseDTO> result = new ArrayList<>();
        for (User user : users) {
            result.add(mapToResponseDTO(user));
        }
        return result;
    }

    // UPDATE
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            ValidationUtil.validateName(request.getFirstName(), "First name");
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            ValidationUtil.validateName(request.getLastName(), "Last name");
            user.setLastName(request.getLastName());
        }

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            ValidationUtil.validateUsername(request.getUsername());
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new DuplicateEntryException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters.");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        }

        return mapToResponseDTO(userRepository.save(user));
    }

    // DELETE
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Soft delete — deactivate instead of removing from DB
        user.setActive(false);
        userRepository.save(user);
    }

    // MAPPER
    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setActive(user.isActive());
        return response;
    }
}