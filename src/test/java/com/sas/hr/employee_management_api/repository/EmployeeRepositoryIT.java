package com.sas.hr.employee_management_api.repository;

import com.sas.hr.employee_management_api.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmployeeRepositoryIT {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerH2Properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
        jdbcTemplate.execute("CREATE TABLE employee (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "first_name VARCHAR(255), " +
                "last_name VARCHAR(255), " +
                "city VARCHAR(255), " +
                "state VARCHAR(255), " +
                "location VARCHAR(255), " +
                "birth_day DATE)");
    }

    @Test
    void testBatchInsertEmployeesUsingJdbc() {
        List<Employee> employees = Arrays.asList(
                new Employee(null, "John", "Doe", "New York", "NY", "HQ", LocalDate.of(1990, 5, 15)),
                new Employee(null, "Jane", "Smith", "Los Angeles", "CA", "Branch", LocalDate.of(1985, 8, 22)),
                new Employee(null, "Bob", "Johnson", "Chicago", "IL", "Branch", null)
        );

        employeeRepository.batchInsertEmployeesUsingJdbc(employees);

        int count = employeeRepository.countAddresses();
        assertEquals(3, count);
    }

    @Test
    void testFindEmployeesByBirthdayMonth() {
        // Insert test data
        List<Employee> employees = Arrays.asList(
                new Employee(null, "John", "Doe", "New York", "NY", "HQ", LocalDate.of(1990, 5, 15)),
                new Employee(null, "Jane", "Smith", "Los Angeles", "CA", "Branch", LocalDate.of(1985, 5, 22)),
                new Employee(null, "Bob", "Johnson", "Chicago", "IL", "Branch", LocalDate.of(1988, 6, 10))
        );
        employeeRepository.batchInsertEmployeesUsingJdbc(employees);

        // Test finding employees born in May
        Page<Employee> mayEmployees = employeeRepository.findEmployeesByBirthdayMonth(5, PageRequest.of(0, 10));

        assertEquals(2, mayEmployees.getContent().size());
        assertTrue(mayEmployees.getContent().stream().allMatch(e -> e.getBirthDay().getMonthValue() == 5));
    }

    @Test
    void testCountAddresses() {
        List<Employee> employees = Arrays.asList(
                new Employee(null, "John", "Doe", "New York", "NY", "HQ", LocalDate.of(1990, 5, 15)),
                new Employee(null, "Jane", "Smith", "Los Angeles", "CA", "Branch", LocalDate.of(1985, 8, 22))
        );
        employeeRepository.batchInsertEmployeesUsingJdbc(employees);

        int count = employeeRepository.countAddresses();
        assertEquals(2, count);
    }
}