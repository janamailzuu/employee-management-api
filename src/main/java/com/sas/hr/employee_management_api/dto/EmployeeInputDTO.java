package com.sas.hr.employee_management_api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeInputDTO(
        @NotBlank(message = "First name cannot be blank")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        String lastName,
        String city,
        String state,
        String location,
        @NotNull(message = "Birth date cannot be null")
        String birthDate   ) {
}