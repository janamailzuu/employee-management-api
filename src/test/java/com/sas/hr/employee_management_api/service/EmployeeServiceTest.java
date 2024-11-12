package com.sas.hr.employee_management_api.service;

import com.sas.hr.employee_management_api.dto.EmployeeDetailsDTO;
import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import com.sas.hr.employee_management_api.exception.EmployeeNotFoundException;
import com.sas.hr.employee_management_api.model.Employee;
import com.sas.hr.employee_management_api.repository.EmployeeJpaRepository;
import com.sas.hr.employee_management_api.repository.EmployeeRepository;
import com.sas.hr.employee_management_api.util.CSVProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CSVProcessor csvProcessor;

    @Mock
    private EmployeeJpaRepository employeeJpaRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void testGetEmployeeByIdSuccess() {

        //Arrange
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
        Employee emp2 = new Employee(2L, "Pal", "Smith","Los Angeles", "CA","Los Angeles, CA", LocalDate.of(1991, 5, 12));
        when(employeeJpaRepository.findById(1L)).thenReturn(Optional.of(emp1));

        //Act
        EmployeeDetailsDTO employeeDetailsDTO = employeeService.getEmployeeById(1L);

        //Assert
        Assertions.assertNotNull(employeeDetailsDTO);
        assertEquals("John",employeeDetailsDTO.firstName());
        assertEquals("Peter",employeeDetailsDTO.lastName());
    }



    @Test
    void testGetAllEmployeesByMonth(){
        //Arrange
        Employee emp2 = new Employee(2L, "Pal", "Smith","Los Angeles", "CA","Los Angeles, CA", LocalDate.of(1991, 5, 12));
        when(employeeRepository.findEmployeesByBirthdayMonth(1, Pageable.ofSize(4).withPage(0))).thenReturn(new PageImpl<>(List.of(emp2)));

        //Act
        Page<EmployeeDetailsDTO> result = employeeService.getAllEmployeesByMonth(1,Pageable.ofSize(4).withPage(0));

        //Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result).extracting(EmployeeDetailsDTO::firstName).contains("Pal");
    }

    @Test
    void testGetAllEmployees(){
        //Arrange
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
        Employee emp2 = new Employee(2L, "Pal", "Smith","Los Angeles", "CA","Los Angeles, CA", LocalDate.of(1991, 5, 12));
        when(employeeJpaRepository.findAll(Pageable.ofSize(4).withPage(0))).thenReturn(new PageImpl<>(List.of(emp1,emp2)));

        //Act
        Page<EmployeeDetailsDTO> result = employeeService.getAllEmployees(Pageable.ofSize(4).withPage(0));

        //Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(EmployeeDetailsDTO::firstName).contains("John");
    }

    @Test
    void testDeleteEmployeeById() {
        //Arrange
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
       when(employeeJpaRepository.findById(1L)).thenReturn(Optional.of(emp1));
        doNothing().when(employeeJpaRepository).delete(emp1);

       //Act
       employeeService.deleteEmployeeById(1L);

       //Assert
        verify(employeeJpaRepository, times(1)).delete(emp1);
    }

    @Test
    void testDeleteEmployeeByIdNotFound() {
        //Arrange
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
                when(employeeJpaRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        assertThatThrownBy(() -> employeeService.deleteEmployeeById(1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found with id: 1");

        // Verify that the deleteById method was never called
        verify(employeeJpaRepository, never()).delete(emp1);

        //Assert
        // Assert that calling get() on an empty Optional throws NoSuchElementException
        assertThatThrownBy(() -> Optional.empty().get())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");
    }

    @Test
    void testCreateEmployee() {
        //ARRANGE
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
        when(employeeJpaRepository.save(Mockito.any(Employee.class))).thenReturn(emp1);


        EmployeeInputDTO employeeInputDTO = new EmployeeInputDTO("John", "Peter", "New York","NY,","New York, NY","10/5/2020");
        //act
        EmployeeDetailsDTO createdEmployee = employeeService.createEmployee(employeeInputDTO);

        //ASSERT
        assertThat(createdEmployee.firstName()).isEqualTo(employeeInputDTO.firstName());
    }

    @Test
    void testUpdateEmployee() {
       //Arrange
        Employee emp1 = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));
        Mockito.when(employeeJpaRepository.findById(1L)).thenReturn(Optional.of(emp1));
        Mockito.when(employeeJpaRepository.save(Mockito.any(Employee.class))).thenReturn(emp1);

        //Act
        EmployeeInputDTO updatedEmployee = new EmployeeInputDTO("Pal", "Smith", "Los Angeles","CA","Los Angeles,CA","10/5/2020");
        EmployeeDetailsDTO result = employeeService.updateEmployee(1L, updatedEmployee);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Pal");
    }

    @Test
    public void testSaveEmployeesFromResources() throws IOException {
        // Arrange
        Resource resource = new ClassPathResource("static/data/ProgrammingChallengeData.csv");

        // Act
        employeeService.saveEmployeesFromResources();

        // Assert
        // Verify that processCsvFile was called with the correct resource
        verify(csvProcessor, times(1)).loadEmployeesFromCsv(resource);
    }

    @Test
    public void testProcessUploadedCsv() throws IOException {
        // Arrange
        String originalFilename = "test.csv";
        byte[] fileContent = "header1,header2\nvalue1,value2".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        // Act
        employeeService.processUploadedCsv(multipartFile);

        // Assert
        // Verify that processCsvFile was called with the correct resource
        String tempFileName = "uploadedFiles/" + originalFilename; // Adjust based on temp directory structure
        Path tempDir = Files.createTempDirectory("uploadedFiles");
        Path tempFilePath = tempDir.resolve(tempFileName);

        Resource resource = new FileSystemResource(tempFilePath.toFile());

        verify(csvProcessor, times(1)).loadEmployeesFromCsv(any(Resource.class));

        // Clean up temporary files (if needed)
        Files.deleteIfExists(tempFilePath);
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testGetEmployeeById_NotExists() {
        // Arrange
        Long employeeId = 2L;
        when(employeeJpaRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById(2L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found with id: 2");

    }

    @Test
    public void testCreateEmployee_DataIntegrityViolation() {
        // Arrange
        EmployeeInputDTO inputDTO = new EmployeeInputDTO("John", "Peter", "New York","NY,","New York, NY","10/5/2020");
        Employee employee = new Employee(1L, "John", "Peter", "New York","NY","New York, NY", LocalDate.of(1985, 5, 25));

        when(employeeJpaRepository.save(any(Employee.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // Act & Assert
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            employeeService.createEmployee(inputDTO);
        });
   }
}
