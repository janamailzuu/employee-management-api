package com.sas.hr.employee_management_api.repository;


import com.sas.hr.employee_management_api.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBatchInsertEmployeesUsingJdbc() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John", "Doe", "New York", "NY", "HQ", LocalDate.of(1990, 5, 15)),
                new Employee(2L, "Jane", "Smith", "Los Angeles", "CA", "Branch", LocalDate.of(1985, 8, 22))
        );

        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1});

        employeeRepository.batchInsertEmployeesUsingJdbc(employees);

        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void testFindEmployeesByBirthdayMonth() {
        int month = 5;
        PageRequest pageable = PageRequest.of(0, 10);

        List<Employee> expectedEmployees = Arrays.asList(
                new Employee(1L, "John", "Doe", "New York", "NY", "HQ", LocalDate.of(1990, 5, 15)),
                new Employee(2L, "Jane", "Smith", "Los Angeles", "CA", "Branch", LocalDate.of(1985, 5, 22))
        );

        when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expectedEmployees);

        when(namedParameterJdbcTemplate.queryForObject(anyString(),anyMap(), eq(Integer.class)))
                .thenReturn(2);

        Page<Employee> result = employeeRepository.findEmployeesByBirthdayMonth(month, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(namedParameterJdbcTemplate, times(1)).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(anyString(),any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    void testCountAddresses() {
        when(namedParameterJdbcTemplate.queryForObject(anyString(),any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(5);

        int count = employeeRepository.countEmployeesByBirthdayMonth(5);

        assertEquals(5, count);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(anyString(),any(MapSqlParameterSource.class), eq(Integer.class));
    }

    @Test
    void testCountAddressesWithNullResult() {
        when(namedParameterJdbcTemplate.queryForObject(anyString(),anyMap(), eq(Integer.class)))
                .thenReturn(null);

        int count = employeeRepository.countEmployeesByBirthdayMonth(5);

        assertEquals(0, count);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(anyString(),any(MapSqlParameterSource.class), eq(Integer.class));
    }
}