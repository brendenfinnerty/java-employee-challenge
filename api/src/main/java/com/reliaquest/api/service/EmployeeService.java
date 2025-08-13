package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmployeeService implements IEmployeeService {

    private final EmployeeClient employeeClient;

    public EmployeeService(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    /**
     * Returns a list of all employees from the data source.
     * <p>
     * Wraps the client call and logs any errors
     * before rethrowing them. In a real application, consider adding caching or
     * pagination if the dataset is large. i.e if employee count is too large, pass in page number as param
     * </p>
     *
     * @return all employees
     */
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
     * Finds employees whose names contain the given search string (case-insensitive).
     * If the search string is null or blank, returns all employees.
     * <p>
     * Note: This filters in memory by loading all employees first, which isn’t the most
     * efficient. In a real application, this should be done with a database query so the
     * filtering happens where the data lives.
     * </p>
     *
     * @param searchString text to search for in employee names
     * @return matching employees, or all employees if search string is empty
     */
    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        try {
            if (searchString == null || searchString.isBlank()) {
                return getAllEmployees();
            }

            String sanitizedName = searchString.toLowerCase().trim();
            return getAllEmployees().stream()
                    .filter(e ->
                            e.getName() != null && e.getName().toLowerCase().contains(sanitizedName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching employees by name containing '{}'", searchString, e);
            throw e;
        }
    }

    /**
     * Looks up a single employee by their ID.
     * <p>
     * Throws {@link EmployeeNotFoundException} if the ID is null, blank, or no
     * matching employee exists. Logs any errors before rethrowing.
     * </p>
     *
     * @param id the employee's ID
     * @return the matching employee
     * @throws EmployeeNotFoundException if no employee is found for the given ID
     */
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

    /**
     * Finds the highest salary among all employees.
     * <p>
     * Retrieves all employees in memory, then filters and compares their salaries.
     * This works fine for small datasets, but for large ones it would be more efficient
     * to let the database calculate the maximum salary directly.
     * </p>
     *
     * @return the highest salary found, or 0 if no employees exist
     */
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

    /**
     * Returns the names of the top ten highest-paid employees.
     * <p>
     * Retrieves all employees in memory, then sorts and limits the list.
     * This works fine for small datasets, but for large ones it would be
     * more efficient to have the database handle sorting and limiting.
     * </p>
     *
     * @return a list of up to ten employee names, highest earners first
     */
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

    /**
     * Creates a new employee using the provided input data.
     * <p>
     * Calls the client to create the employee and returns the created record.
     * Throws an exception if creation fails or the client returns {@code null}.
     * </p>
     *
     * @param newEmployee details for the new employee
     * @return the created employee
     * @throws IllegalStateException if the client returns {@code null}
     */
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

    /**
     * Deletes an employee by their ID.
     * <p>
     * Throws {@link EmployeeNotFoundException} if the ID is null or blank.
     * Logs any errors before rethrowing them.
     * </p>
     *
     * @param id the employee's ID
     * @return the ID of the deleted employee
     * @throws EmployeeNotFoundException if the ID is null or blank
     */
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
