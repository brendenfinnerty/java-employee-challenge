package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService implements IEmployeeService {

    private final EmployeeClient employeeClient;

    public EmployeeService(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @Override
    public List<Employee> getAllEmployees() {
        try {
            return employeeClient.getAllEmployees();
        } catch (Exception e) {
            log.error("Error getting all employees", e);
            throw e;
        }
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        try {
            if (searchString == null || searchString.isBlank()) {
                return getAllEmployees();
            }

            String needle = searchString.toLowerCase();
            return getAllEmployees().stream()
                    .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(needle))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching employees by name containing '{}'", searchString, e);
            throw e;
        }
    }

    @Override
    public Employee getEmployeeById(String id) {
        try {
            if (id == null || id.isBlank()) {
                throw new EmployeeNotFoundException(String.valueOf(id));
            }

            Employee e = employeeClient.getEmployeeById(id);
            if (e == null) {
                throw new EmployeeNotFoundException(id);
            }
            return e;
        } catch (Exception ex) {
            log.error("Error getting employee with id {}", id, ex);
            throw ex;
        }
    }

    @Override
    public Integer getHighestSalary() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElseGet(() -> {
                    log.warn("No employees found - returning default salary of 0");
                    return 0;
                });
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(employee -> employee.getSalary() != null)
                .sorted((e1, e2) -> Integer.compare(e2.getSalary(), e1.getSalary())) // highest first
                .limit(10)
                .map(Employee::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput newEmployee) {
        try {
            Employee created = employeeClient.createEmployee(newEmployee);
            if (created == null) {
                throw new IllegalStateException("Failed to create employee - client returned null");
            }
            return created;
        } catch (Exception e) {
            log.error("Error creating employee: {}", newEmployee, e);
            throw e;
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        try {
            if (id == null || id.isBlank()) {
                throw new EmployeeNotFoundException(id);
            }
            employeeClient.deleteEmployeeById(id);
            return id;
        } catch (Exception e) {
            log.error("Error deleting employee with id {}", id, e);
            throw e;
        }
    }
}
