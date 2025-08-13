package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.IEmployeeService;
import com.reliaquest.server.model.MockEmployee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class ApiApplicationTest {

    // Replace the real RestTemplate bean in the Spring context with a Mockito mock
    @MockBean
    RestTemplate http;

    @Autowired
    IEmployeeService employeeService; // Autowire your real service bean

    @Test
    void contextLoads_andServiceCanFetchEmployees() {
        // Arrange a tiny fake response from the mock server
        MockEmployee m = org.mockito.Mockito.mock(MockEmployee.class);
        UUID id = UUID.randomUUID();
        when(m.getId()).thenReturn(id);
        when(m.getName()).thenReturn("Brenden");
        when(m.getSalary()).thenReturn(120000);
        when(m.getAge()).thenReturn(30);
        when(m.getTitle()).thenReturn("Developer");
        when(m.getEmail()).thenReturn("brenden@example.com");

        when(http.getForObject(anyString(), eq(MockEmployee[].class))).thenReturn(new MockEmployee[] {m});

        // Act
        List<Employee> result = employeeService.getAllEmployees();

        // Assert (proves context booted, wiring worked, and mapping occurred)
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id.toString(), result.get(0).getId());
        assertEquals("Brenden", result.get(0).getName());
    }
}
