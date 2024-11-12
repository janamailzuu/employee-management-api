package com.sas.hr.employee_management_api.mapper;

import com.sas.hr.employee_management_api.dto.EmployeeDetailsDTO;
import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import com.sas.hr.employee_management_api.model.Employee;
import com.sas.hr.employee_management_api.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EmployeeMapper {

    // Convert DTO to Model (Employee)
    public static Employee toEmployeeEntity(EmployeeInputDTO employeeInputDTO) {
            Employee employee = new Employee();
            employee.setBirthDay(DateUtil.convertDateStringToFormattedLocalDate(employeeInputDTO.birthDate()));
            employee.setFirstName(employeeInputDTO.firstName());
            employee.setLastName(employeeInputDTO.lastName());
            employee.setCity(employeeInputDTO.city());
            employee.setState(employeeInputDTO.state());
            employee.setLocation(employeeInputDTO.location());
        return employee;
    }

//     Convert Model (Employee) to DTO
    public static EmployeeDetailsDTO toEmployeeDTO(Employee employee) {
        String birthDate = DateUtil.formatBirthDate(employee.getBirthDay());
        return new EmployeeDetailsDTO(employee.getId(),employee.getFirstName(), employee.getLastName(),employee.getCity(),employee.getState(), employee.getLocation(), birthDate);
    }

    // List-to-List conversion
    public static List<EmployeeDetailsDTO> toEmployeeDTOList(List<Employee> employees) {
        if (employees == null) {
            return null;
        }

        List<EmployeeDetailsDTO> employeeDTOList = new ArrayList<>();
        for (Employee employee : employees) {
            employeeDTOList.add(toEmployeeDTO(employee));  // Convert each entity to DTO
        }

        return employeeDTOList;
    }

    public static Page<EmployeeDetailsDTO> convertPageEmployeeToDTO(Page<Employee> employeePage) {
        List<EmployeeDetailsDTO> dtoList = employeePage.getContent().stream()
                .map(EmployeeMapper::toEmployeeDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, employeePage.getPageable(), employeePage.getTotalElements());
    }

    public static List<Employee> toEmployeeEntityList(List<EmployeeInputDTO> employeeDTOs) {
        if (employeeDTOs == null) {
            return null;
        }

        List<Employee> employeeList = new ArrayList<>();
        for (EmployeeInputDTO employeeDTO : employeeDTOs) {
            employeeList.add(toEmployeeEntity(employeeDTO));  // Convert each DTO to entity
        }

        return employeeList;
    }
}
