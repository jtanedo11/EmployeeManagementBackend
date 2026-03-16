package com.example.employeemanagementbackend.service;

import com.example.employeemanagementbackend.dto.LoginRequestDTO;
import com.example.employeemanagementbackend.dto.LoginResponseDTO;
import com.example.employeemanagementbackend.entity.User;
import com.example.employeemanagementbackend.exception.ResourceNotFoundException;
import com.example.employeemanagementbackend.repository.UserRepository;
import com.example.employeemanagementbackend.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public LoginResponseDTO login(LoginRequestDTO request) {

        if(request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if(request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if(request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        User existingUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        String token = jwtTokenUtil.generateToken(existingUser.getUsername());
        return new LoginResponseDTO(token, "Login successful");
    }

    public String logout() {
        return "Logged out successfully";
    }
}
