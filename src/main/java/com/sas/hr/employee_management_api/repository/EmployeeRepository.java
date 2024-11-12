package com.sas.hr.employee_management_api.repository;


import com.sas.hr.employee_management_api.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class EmployeeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void batchInsertEmployeesUsingJdbc(List<Employee> employees) {
        String sql = "INSERT INTO employee (first_name, last_name, city, state, location, birth_day) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Employee employee = employees.get(i);
                    ps.setString(1, employee.getFirstName());
                    ps.setString(2, employee.getLastName());
                    ps.setString(3,employee.getCity());
                    ps.setString(4,employee.getState());
                    ps.setString(5, employee.getLocation());
                    ps.setDate(6, employee.getBirthDay() != null ? Date.valueOf(employee.getBirthDay()) : null);
                }
                @Override
                public int getBatchSize() {
                    return employees.size();
                }
            });
        }catch (DataAccessException ex){
            log.error("Error occurred while performing batch insert: {}", ex.getMessage(), ex);
            throw new RuntimeException("Batch insert failed due to database access error.", ex);
        }
    }

    private Employee toEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setLocation(rs.getString("location"));
        employee.setBirthDay(rs.getDate("birth_day").toLocalDate());
        return employee;
    }


    public Page<Employee> findEmployeesByBirthdayMonth(int month, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource("month", month).addValue("limit", pageable.getPageSize()).addValue("offset", (pageable.getPageNumber() * pageable.getPageSize()));
        String sql = "SELECT * FROM employee e WHERE EXTRACT(MONTH FROM e.birth_day) = :month  LIMIT :limit OFFSET :offset";
        try{
            List<Employee> result = namedParameterJdbcTemplate.query(sql, params ,(resultSet, i) -> {
                return toEmployee(resultSet);
            });
            int totalRecords = countEmployeesByBirthdayMonth(month);

            return new PageImpl<>(result, pageable, totalRecords);
        }catch (DataAccessException ex){
            log.error("Error executing findEmployeesByBirthdayMonth query", ex);
            throw new RuntimeException("Failed to retrieve employees by birthday month", ex);

        }

    }

    public int countEmployeesByBirthdayMonth(int month) {
        String sql = "SELECT COUNT(*) FROM employee WHERE EXTRACT(MONTH FROM birth_day) = :month";
        MapSqlParameterSource params = new MapSqlParameterSource("month", month);
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return (count != null) ? count : 0;
    }


}
