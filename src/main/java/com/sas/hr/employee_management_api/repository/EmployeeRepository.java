package com.sas.hr.employee_management_api.repository;


import com.sas.hr.employee_management_api.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
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

@Repository
public class EmployeeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void batchInsertEmployeesUsingJdbc(List<Employee> employees) {
        String sql = "INSERT INTO employee (first_name, last_name, city, state, location, birth_day) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Employee employee = employees.get(i);
                ps.setString(1, employee.getFirstName());
                ps.setString(2, employee.getLastName());
                ps.setString(3,employee.getCity());
                ps.setString(4,employee.getState());

                ps.setString(5, employee.getLocation());
                if(null!=employee.getBirthDay()) {
                    ps.setDate(6, Date.valueOf(employee.getBirthDay()));
                }else{
                    ps.setDate(6,null);
                }
            }

            @Override
            public int getBatchSize() {
                return employees.size();
            }
        });
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
        List<Employee> result = namedParameterJdbcTemplate.query(sql, params ,(resultSet, i) -> {
            return toEmployee(resultSet);
        });
        int totalRecords = countAddresses();

        return new PageImpl<>(result, pageable, totalRecords);
    }

    public int countAddresses() {
        String sql = "SELECT COUNT(*) FROM employee";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class);
        return (count != null) ? count : 0;
    }


}
