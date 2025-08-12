package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeClient client; // concrete client

    @InjectMocks
    private EmployeeService service;

    // -------- getAllEmployees --------

    @Test
    void getAllEmployees_returnsList() {
        Employee e1 = new Employee(UUID.randomUUID(), "Brenden", 120000, 30, "Dev", "b@example.com");
        Employee e2 = new Employee(UUID.randomUUID(), "Finnerty", 130000, 31, "Sr Dev", "f@example.com");
        when(client.getAllEmployees()).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals(e1.getId(), result.get(0).getId());
        assertEquals(e2.getId(), result.get(1).getId());
    }

    @Test
    void getAllEmployees_empty_returnsEmpty() {
        when(client.getAllEmployees()).thenReturn(List.of());
        assertTrue(service.getAllEmployees().isEmpty());
    }

    // -------- getEmployeesByNameSearch --------

    @Test
    void getEmployeesByNameSearch_caseInsensitive() {
        Employee a = new Employee(UUID.randomUUID(), "Alice Johnson", 100, 25, "X", "a@x");
        Employee b = new Employee(UUID.randomUUID(), "Bob", 200, 26, "Y", "b@y");
        when(client.getAllEmployees()).thenReturn(List.of(a, b));

        List<Employee> res = service.getEmployeesByNameSearch("alice");
        assertEquals(1, res.size());
        assertEquals("Alice Johnson", res.get(0).getName());
    }

    // If your service guards null names, this should pass; otherwise add a null-check in filter.
    @Test
    void getEmployeesByNameSearch_nullNameDoesNotNPE() {
        Employee a = new Employee(UUID.randomUUID(), null, 100, 25, "X", "a@x");
        when(client.getAllEmployees()).thenReturn(List.of(a));

        // Expect either [] or guarded behavior — but definitely no exception
        assertDoesNotThrow(() -> service.getEmployeesByNameSearch("x"));
    }

    // If you decide null/blank search returns all, this asserts that.
    @Test
    void getEmployeesByNameSearch_blankReturnsAll() {
        List<Employee> list = List.of(
                new Employee(UUID.randomUUID(), "A", 1, 20, "T", "a@x"),
                new Employee(UUID.randomUUID(), "B", 2, 21, "T", "b@x")
        );
        when(client.getAllEmployees()).thenReturn(list);

        assertEquals(2, service.getEmployeesByNameSearch("  ").size());
    }

    // -------- getEmployeeById --------

    @Test
    void getEmployeeById_found() {
        UUID id = UUID.randomUUID();
        Employee e = new Employee(id, "Brenden", 120000, 30, "Dev", "b@example.com");
        when(client.getEmployeeById(id.toString())).thenReturn(e);

        Employee result = service.getEmployeeById(id.toString());
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    // If your service throws when not found, this is correct. If it returns null, change to assertNull.
    @Test
    void getEmployeeById_notFound_throws() {
        when(client.getEmployeeById(anyString())).thenReturn(null);
        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById("missing-id"));
    }

    // -------- getHighestSalary --------

    @Test
    void getHighestSalary_happyPath() {
        Employee a = new Employee(UUID.randomUUID(), "A", 150, 25, "X", "a@x");
        Employee b = new Employee(UUID.randomUUID(), "B", 200, 26, "Y", "b@y");
        when(client.getAllEmployees()).thenReturn(List.of(a, b));
        assertEquals(200, service.getHighestSalary());
    }

    @Test
    void getHighestSalary_empty_returnsZero() {
        when(client.getAllEmployees()).thenReturn(List.of());
        assertEquals(0, service.getHighestSalary());
    }

    // If your service filters null salaries, this should pass; otherwise add filter(Objects::nonNull).
    @Test
    void getHighestSalary_allNullSalaries_returnsZero() {
        Employee a = new Employee(UUID.randomUUID(), "A", null, 25, "X", "a@x");
        Employee b = new Employee(UUID.randomUUID(), "B", null, 26, "Y", "b@y");
        when(client.getAllEmployees()).thenReturn(List.of(a, b));
        assertEquals(0, service.getHighestSalary());
    }

    // -------- getTopTenHighestEarningEmployeeNames --------

    @Test
    void topTenHighestEarningEmployeeNames_sortedDesc_limit10() {
        List<Employee> many = new ArrayList<>();
        IntStream.range(0, 15).forEach(i ->
                many.add(new Employee(UUID.randomUUID(), "E" + i, 100 + i, 30, "T", "e@x"))
        );
        when(client.getAllEmployees()).thenReturn(many);

        List<String> names = service.getTopTenHighestEarningEmployeeNames();
        assertEquals(10, names.size());
        assertEquals("E14", names.get(0)); // highest salary first
        assertEquals("E5", names.get(9));  // 10th item
    }

    // If your service skips null salaries, ensure it still returns some names.
    @Test
    void topTenHighestEarningEmployeeNames_handlesNulls() {
        List<Employee> list = List.of(
                new Employee(UUID.randomUUID(), "A", null, 25, "X", "a@x"),
                new Employee(UUID.randomUUID(), "B", 200, 26, "Y", "b@y")
        );
        when(client.getAllEmployees()).thenReturn(list);

        List<String> names = service.getTopTenHighestEarningEmployeeNames();
        assertEquals(1, names.size());
        assertEquals("B", names.get(0));
    }

    // -------- createEmployee --------

    @Test
    void createEmployee_happyPath_returnsCreated() {
        CreateEmployeeInput in = new CreateEmployeeInput("Brenden", 120000, 30, "Dev");
        Employee created = new Employee(UUID.randomUUID(), "Brenden", 120000, 30, "Dev", "b@example.com");
        when(client.createEmployee(any(CreateEmployeeInput.class))).thenReturn(created);

        Employee result = service.createEmployee(in);
        assertNotNull(result);
        assertEquals("Brenden", result.getName());
        assertNotNull(result.getId());
    }

    // If your service throws on null create, keep this; otherwise change to assertNull.
    @Test
    void createEmployee_clientReturnsNull_throwsIllegalState() {
        CreateEmployeeInput in = new CreateEmployeeInput("Brenden", 120000, 30, "Dev");
        when(client.createEmployee(any(CreateEmployeeInput.class))).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.createEmployee(in));
    }

    // -------- deleteEmployeeById --------

    @Test
    void deleteEmployeeById_nullId_throwsNotFound() {
        assertThrows(EmployeeNotFoundException.class, () -> service.deleteEmployeeById(null));
    }

    @Test
    void deleteEmployeeById_blankId_throwsNotFound() {
        assertThrows(EmployeeNotFoundException.class, () -> service.deleteEmployeeById("   "));
    }

    @Test
    void deleteEmployeeById_happyPath_returnsConfirmation() {
        UUID id = UUID.randomUUID();
        when(client.deleteEmployeeById(id.toString())).thenReturn(id.toString());

        String result = service.deleteEmployeeById(id.toString());
        assertEquals(id.toString(), result);
    }
}