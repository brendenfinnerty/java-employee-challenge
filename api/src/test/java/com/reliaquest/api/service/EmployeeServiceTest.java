package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.server.model.MockEmployee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    RestTemplate http;

    @InjectMocks
    EmployeeService service; // concrete impl that implements IEmployeeService

    @Test
    void getAllEmployees_mapsMockEmployeesToApiEmployees(){
        MockEmployee e1 = org.mockito.Mockito.mock(MockEmployee.class);
        MockEmployee e2 = org.mockito.Mockito.mock(MockEmployee.class);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(e1.getId()).thenReturn(id1);
        when(e1.getName()).thenReturn("Brenden Finnerty");
        when(e1.getSalary()).thenReturn(120000);
        when(e1.getAge()).thenReturn(30);
        when(e1.getTitle()).thenReturn("Developer");
        when(e1.getEmail()).thenReturn("brenden.finnerty@gmail.com");

        when(e2.getId()).thenReturn(id2);
        when(e2.getName()).thenReturn("Nicolas Cage");
        when(e2.getSalary()).thenReturn(20000000);
        when(e2.getAge()).thenReturn(61);
        when(e2.getTitle()).thenReturn("Actor");
        when(e2.getEmail()).thenReturn("thecage@aol.com");

        when(http.getForObject(anyString(), eq(MockEmployee[].class)))
                .thenReturn(new MockEmployee[]{ e1, e2 });

        // Act
        List<Employee> result = service.getAllEmployees();

        // Assert
        assertEquals(2, result.size());

        Employee r1 = result.get(0);
        Employee r2 = result.get(1);

        assertEquals(id1.toString(), r1.getId());
        assertEquals("Brenden Finnerty", r1.getName());
        assertEquals(120000, r1.getSalary());
        assertEquals(30, r1.getAge());
        assertEquals("Developer", r1.getTitle());
        assertEquals("brenden.finnerty@gmail.com", r1.getEmail());

        assertEquals(id2.toString(), r2.getId());
        assertEquals("Nicolas Cage", r2.getName());
        assertEquals(20000000, r2.getSalary());
        assertEquals(61, r2.getAge());
        assertEquals("Actor", r2.getTitle());
        assertEquals("thecage@aol.com", r2.getEmail());
    }
}
