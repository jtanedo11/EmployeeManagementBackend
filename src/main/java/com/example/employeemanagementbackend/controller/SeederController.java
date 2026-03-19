package com.example.employeemanagementbackend.controller;

import com.example.employeemanagementbackend.seed.EmployeeManagementSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("dev")
public class SeederController {

    private final EmployeeManagementSeeder seeder;

    @PostMapping("/seed")
    public ResponseEntity<String> seed() {
        return ResponseEntity.ok(seeder.reseedDatabase());
    }
}