package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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

    /**
     * Filters employees in memory after fetching all from the API.
     * Normally this should be handled at the DB/API level for efficiency,
     * but this is the best option given current API constraints.
     */
    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        try {
            List<Employee> employees = getAllEmployees();
            return employees.stream()
                    .filter(employee -> employee.getName().toLowerCase()
                            .contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching employees by name containing '{}'", searchString, e);
            throw e;
        }
    }

    @Override
    public Employee getEmployeeById(String id) {
        try {
            return employeeClient.getEmployeeById(id);
        } catch (Exception e) {
            log.error("Error getting employee with id {}", id, e);
            throw e;
        }
    }

    /**
     * Retrieves the highest salary among all employees.
     * <p>
     * If no employees are found, logs a warning and returns {@code 0} as a default value.
     * This fallback is necessary because we cannot guarantee the API will always return data.
     *
     * @return the highest salary, or {@code 0} if no employees exist
     */
    @Override
    public Integer getHighestSalary() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .map(Employee::getSalary)
                .max(Integer::compareTo)
                .orElseGet(() -> {
                    log.warn("No employees found — returning default salary of 0");
                    return 0;
                });
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getSalary(), e1.getSalary())) // highest first
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput input) {
        try {
            return employeeClient.createEmployee(input);
        } catch (Exception e) {
            log.error("Error creating employee: {}", input.getName(), e);
            throw e;
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        try {
            if (id == null || id.isEmpty()) {
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
