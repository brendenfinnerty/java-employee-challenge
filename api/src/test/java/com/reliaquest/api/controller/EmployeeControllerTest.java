package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.IEmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private MockMvc mvc;

    @MockBean
    private IEmployeeService employeeService;

    @Test
    void getAllEmployees_returnsOkWithBody() throws Exception {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Employee e1 = new Employee();
        e1.setId(id1);
        e1.setName("Brenden");
        e1.setSalary(120000);
        e1.setAge(30);
        e1.setTitle("Developer");
        e1.setEmail("b@example.com");

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1));

        mvc.perform(get("/api/v2/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].name").value("Brenden"))
                .andExpect(jsonPath("$[0].salary").value(120000))
                .andExpect(jsonPath("$[0].title").value("Developer"));
    }

    @Test
    void getEmployeeById_returnsOk() throws Exception {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Employee e = new Employee();
        e.setId(id);
        e.setName("Brenden");

        when(employeeService.getEmployeeById(id.toString())).thenReturn(e);

        mvc.perform(get("/api/v2/employee/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Brenden"));
    }

    @Test
    void highestSalary_returnsOk() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(120000);

        mvc.perform(get("/api/v2/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("120000"));
    }

    @Test
    void topTenHighestEarningEmployeeNames_returnsOk() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenReturn(List.of("Brenden", "Finnerty"));

        mvc.perform(get("/api/v2/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Brenden"))
                .andExpect(jsonPath("$[1]").value("Finnerty"));
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        CreateEmployeeInput in = new CreateEmployeeInput("Brenden", 120000, 30, "Developer");

        UUID newId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Employee created = new Employee();
        created.setId(newId);
        created.setName("Brenden");

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(created);

        mvc.perform(post("/api/v2/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newId.toString()))
                .andExpect(jsonPath("$.name").value("Brenden"));
    }

    @Test
    void deleteEmployeeById_returnsOkMessage() throws Exception {
        UUID id = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(employeeService.deleteEmployeeById(eq(id.toString()))).thenReturn(id.toString());

        mvc.perform(delete("/api/v2/employee/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(id.toString()));
    }
}