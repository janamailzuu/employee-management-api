package com.sas.hr.employee_management_api.dto;


public record EmployeeDetailsDTO(Long id,
                                 String firstName,
                                 String lastName,
                                 String city,
                                 String state,
                                 String location,
                                 String birthDate) {
}
