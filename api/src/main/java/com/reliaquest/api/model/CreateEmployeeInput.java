package com.reliaquest.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Creating a separate model to represent the payload a client sends when creating a new employee.
 * I am keeping this separate from the Employee model so that we don't force the client to send system-generated fields
 * like email or id. I think this will make the API contract cleaner.
 */

@Setter
@Getter
public class CreateEmployeeInput {
    private String name;
    private Integer salary;
    private Integer age;
    private String title;

}
