package com.example.employeemanagementbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Long departmentId;
    private BigDecimal salary;
}
