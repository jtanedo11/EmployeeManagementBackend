package com.example.employeemanagementbackend.controller;

import com.example.employeemanagementbackend.dto.UserRequestDTO;
import com.example.employeemanagementbackend.dto.UserResponseDTO;
import com.example.employeemanagementbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ADD
    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // SEARCH BY USERNAME
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.searchByUsername(username));
    }

    // FILTER BY ROLE
    @GetMapping("/filter")
    public ResponseEntity<List<UserResponseDTO>> filterByRole(@RequestParam String role) {
        return ResponseEntity.ok(userService.filterByRole(role));
    }

    // COMBINED SEARCH + FILTER
    @GetMapping("/search-filter")
    public ResponseEntity<List<UserResponseDTO>> searchAndFilter(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.searchAndFilter(username, role));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
}