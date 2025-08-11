package com.reliaquest.api.service;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.server.model.MockEmployee;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class EmployeeService implements IEmployeeService {

    private static final String BASE = "http://localhost:8112/api/v1/employee";

    private final RestTemplate http;

    public EmployeeService(RestTemplate http) {
        this.http = http;
    }

    @Override
    public List<Employee> getAllEmployees() {
        MockEmployee[] mockEmployees = withRetry(() ->
                http.getForObject(BASE, MockEmployee[].class)
        );

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

    private <T> T withRetry(Supplier<T> request) {
        int attempts = 0;
        long backoff = 200;
        while (true) {
            try {
                return request.get();
            } catch (HttpStatusCodeException ex) {
                if (ex.getStatusCode().value() == 429 && attempts < 3) {
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ignored) {
                    }
                    attempts++;
                    backoff *= 2;
                } else {
                    throw ex;
                }
            }
        }
    }
}
