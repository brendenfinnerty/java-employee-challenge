package com.reliaquest.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class EmployeeClient {

    private static final String BASE = "http://localhost:8112/api/v1/employee";
    private final RestTemplate http;

    public EmployeeClient(RestTemplate http) {
        this.http = http;
    }

    /**
     * Fetch all employees from the mock server and returns API model.
     */
    public List<Employee> getAllEmployees() {
        ResponseEntity<ApiResponse<List<Employee>>> resp = withRetry(() -> http.exchange(
                BASE, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {}));
        ApiResponse<List<Employee>> body = resp.getBody();
        return (body == null || body.getData() == null) ? List.of() : body.getData();
    }

    /**
     * Fetch one employee by id. Returns null if the mock returns no data.
     */
    public Employee getEmployeeById(String id) {
        ResponseEntity<ApiResponse<Employee>> resp = withRetry(() -> http.exchange(
                BASE + "/" + id, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<Employee>>() {}));
        ApiResponse<Employee> body = resp.getBody();
        return body == null ? null : body.getData();
    }

    /**
     * Create an employee using API input. Returns the created Employee
     */
    public Employee createEmployee(CreateEmployeeInput input) {
        ResponseEntity<ApiResponse<Employee>> resp = withRetry(() -> http.exchange(
                BASE,
                HttpMethod.POST,
                new HttpEntity<>(input),
                new ParameterizedTypeReference<ApiResponse<Employee>>() {}));
        ApiResponse<Employee> body = resp.getBody();
        return body == null ? null : body.getData();
    }

    /**
     * Delete by id.
     * If the mock server expects DELETE with a JSON body { "name": "..."}, we first resolve the id to get the name.
     * Returns a confirmation string (or null if nothing came back).
     */
    public String deleteEmployeeById(String id) {
        Employee e = getEmployeeById(id);
        if (e == null || e.getName() == null) return null;

        ResponseEntity<ApiResponse<String>> resp = withRetry(() -> http.exchange(
                BASE,
                HttpMethod.DELETE,
                new HttpEntity<>(new NameDeleteBody(e.getName())),
                new ParameterizedTypeReference<ApiResponse<String>>() {}));
        ApiResponse<String> body = resp.getBody();
        return body == null ? null : body.getData();
    }

    /**
     * Matches the mock server envelope: { "data": ... } (other fields ignored).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        private T data;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    /**
     * Used when the mock server expects DELETE with a JSON body containing the employee name.
     */
    private static class NameDeleteBody {
        public String name;

        NameDeleteBody(String name) {
            this.name = name;
        }
    }

    // --- retry helper for transient 429s ---
    private <T> T withRetry(Supplier<T> call) {
        int attempts = 0;
        long backoff = 200;
        while (true) {
            try {
                return call.get();
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
