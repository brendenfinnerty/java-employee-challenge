package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.server.model.MockEmployee;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService implements IEmployeeService {

    private final EmployeeClient employeeClient;

    public EmployeeService(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }


    @Override
    public List<Employee> getAllEmployees() {
        MockEmployee[] mockEmployees = employeeClient.getAllEmployees();

        if (mockEmployees == null || mockEmployees.length == 0) {
            return List.of();
        }

        return Arrays.stream(mockEmployees)
                .map(this::toEmployee)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        return List.of();
    }

    @Override
    public Employee getEmployeeById(String id) {
        return null;
    }

    @Override
    public Integer getHighestSalary() {
        return 0;
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return List.of();
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput input) {
        return null;
    }

    @Override
    public String deleteEmployeeById(String id) {
        return "";
    }

    //Helpers

    private Employee toEmployee(MockEmployee m) {
        Employee e = new Employee();
        e.setId(m.getId() != null ? m.getId().toString() : null);
        e.setName(m.getName());
        e.setSalary(m.getSalary());
        e.setAge(m.getAge());
        e.setTitle(m.getTitle());
        e.setEmail(m.getEmail());
        return e;
    }
}
