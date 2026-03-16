// DuplicateEntryException.java
package com.example.employeemanagementbackend.exception;

public class DuplicateEntryException extends RuntimeException {
    public DuplicateEntryException(String message) {
        super(message);
    }
}