package com.example.employeemanagementbackend.controller;

import com.example.employeemanagementbackend.dto.LoginRequestDTO;
import com.example.employeemanagementbackend.dto.LoginResponseDTO;
import com.example.employeemanagementbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.auth.base}")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("${api.auth.login}")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("${api.auth.logout}")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(authService.logout());
    }
}