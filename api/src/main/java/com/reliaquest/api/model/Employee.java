package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private UUID id;

    @JsonAlias("employee_name")
    private String name;

    @JsonAlias("employee_salary")
    private Integer salary;

    @JsonAlias("employee_age")
    private Integer age;

    @JsonAlias("employee_title")
    private String title;

    @JsonAlias("employee_email")
    private String email;
}
