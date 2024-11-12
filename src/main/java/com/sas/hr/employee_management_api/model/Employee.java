package com.sas.hr.employee_management_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee")
@SqlResultSetMapping(
        name = "EmployeeMapping",
        entities = @EntityResult(entityClass = Employee.class)
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name")
    private String firstName;
    @Column(name="last_name")
    private String lastName;
    @Column(name="location")
    private String location;

    @Column
    private String city;

    @Column
    private String state;

    @Column(name="birth_day")
    private LocalDate birthDay;
}
