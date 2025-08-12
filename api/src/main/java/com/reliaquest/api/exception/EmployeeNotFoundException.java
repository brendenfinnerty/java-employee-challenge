package com.reliaquest.api.exception;

public class EmployeeNotFoundException extends EmployeeApiException {
    public EmployeeNotFoundException(String id) {
        super("Employee with id '" + id + "' was not found.");
    }
}
