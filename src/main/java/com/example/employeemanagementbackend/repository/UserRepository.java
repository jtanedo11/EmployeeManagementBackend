package com.example.employeemanagementbackend.repository;

import com.example.employeemanagementbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // For Login
    Optional<User> findByUsername(String username);
}
