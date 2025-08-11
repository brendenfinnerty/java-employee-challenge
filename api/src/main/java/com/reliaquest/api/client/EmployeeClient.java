package com.reliaquest.api.client;

import com.reliaquest.server.model.MockEmployee;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Component
public class EmployeeClient {

    private static final String BASE = "http://localhost:8112/api/v1/employee";
    private final RestTemplate http;

    public EmployeeClient(RestTemplate http) {
        this.http = http;
    }

    public MockEmployee[] getAllEmployees() {
        return withRetry(() -> http.getForObject(BASE, MockEmployee[].class));
    }

    public MockEmployee getEmployeeById(String id) {
        return withRetry(() -> http.getForObject(BASE + "/" + id, MockEmployee.class));
    }

    public MockEmployee createEmployee(MockEmployee newEmployee) {
        return withRetry(() -> http.postForObject(BASE, newEmployee, MockEmployee.class));
    }

    public void deleteEmployeeById(String id) {
        withRetry(() -> {
            http.delete(BASE + "/" + id);
            return null; // delete returns void, so return null here
        });
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
                    } catch (InterruptedException ignored) {}
                    attempts++;
                    backoff *= 2;
                } else {
                    throw ex;
                }
            }
        }
    }
}