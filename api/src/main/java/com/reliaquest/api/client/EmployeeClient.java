package com.reliaquest.api.client;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Supplier;

@Component
public class EmployeeClient {

    private static final String BASE = "http://localhost:8112/api/v1/employee";
    private final RestTemplate http;

    public EmployeeClient(RestTemplate http) {
        this.http = http;
    }

    /** Returns API Employees; deserializes mock payload internally and maps. */
    public List<Employee> getAllEmployees() {
        ResponseEntity<Response<List<MockEmployee>>> resp = withRetry(() ->
                http.exchange(
                        BASE,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Response<List<MockEmployee>>>() {}
                )
        );
        List<MockEmployee> data = resp.getBody() != null ? resp.getBody().data() : List.of();
        return data == null ? List.of() : data.stream().map(this::toEmployee).toList();
    }

    public Employee getEmployeeById(String id) {
        ResponseEntity<Response<MockEmployee>> resp = withRetry(() ->
                http.exchange(
                        BASE + "/" + id,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Response<MockEmployee>>() {}
                )
        );
        MockEmployee m = resp.getBody() != null ? resp.getBody().data() : null;
        return m == null ? null : toEmployee(m);
    }

    public Employee createEmployee(CreateEmployeeInput input) {
        CreateMockEmployeeInput body = new CreateMockEmployeeInput();
        body.setName(input.getName());
        body.setSalary(input.getSalary());
        body.setAge(input.getAge());
        body.setTitle(input.getTitle());

        ResponseEntity<Response<MockEmployee>> resp = withRetry(() ->
                http.exchange(
                        BASE,
                        HttpMethod.POST,
                        new HttpEntity<>(body),
                        new ParameterizedTypeReference<Response<MockEmployee>>() {}
                )
        );
        MockEmployee created = resp.getBody() != null ? resp.getBody().data() : null;
        return created == null ? null : toEmployee(created);
    }

    public String deleteEmployeeById(String id) {
        Employee e = getEmployeeById(id);
        if (e == null || e.getName() == null) return null;

        DeleteMockEmployeeInput body = new DeleteMockEmployeeInput();
        body.setName(e.getName());

        ResponseEntity<Response<String>> resp = withRetry(() ->
                http.exchange(
                        BASE,
                        HttpMethod.DELETE,
                        new HttpEntity<>(body),
                        new ParameterizedTypeReference<Response<String>>() {}
                )
        );
        Response<String> wrapper = resp.getBody();
        return (wrapper != null && wrapper.data() != null) ? wrapper.data() : id;
    }

    // Map server model -> API model (internal only)
    private Employee toEmployee(MockEmployee m) {
        Employee e = new Employee();
        e.setId(m.getId());     // UUID -> UUID
        e.setName(m.getName());
        e.setSalary(m.getSalary());
        e.setAge(m.getAge());
        e.setTitle(m.getTitle());
        e.setEmail(m.getEmail());
        return e;
    }

    // Retry 429s with simple backoff
    private <T> T withRetry(Supplier<T> call) {
        int attempts = 0;
        long backoff = 200;
        while (true) {
            try {
                return call.get();
            } catch (HttpStatusCodeException ex) {
                if (ex.getStatusCode().value() == 429 && attempts < 3) {
                    try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                    attempts++;
                    backoff *= 2;
                } else {
                    throw ex;
                }
            }
        }
    }
}