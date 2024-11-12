package com.sas.hr.employee_management_api.controller;

import com.sas.hr.employee_management_api.dto.EmployeeDetailsDTO;
import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import com.sas.hr.employee_management_api.exception.EmployeeNotFoundException;
import com.sas.hr.employee_management_api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        PageableHandlerMethodArgumentResolver pageableArgumentResolver = new PageableHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .build();
    }
    @Test
    public void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        // Arrange
        Long employeeId = 1L;
        EmployeeDetailsDTO employeeDetailsDTO = new EmployeeDetailsDTO(1L, "John", "Peter", "New York","NY","New York, NY", "1985-05-05");
        // Populate with test data
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeDetailsDTO);

        // Act & Assert
        mockMvc.perform(get("/employees/{id}", employeeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employeeId)); // Adjust based on your DTO structure

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    public void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        // Arrange
        EmployeeInputDTO inputDto = new EmployeeInputDTO("John", "Doe", "New York", "NY", "USA", "1990-01-01");
        EmployeeDetailsDTO createdEmployee = new EmployeeDetailsDTO(1L, "John", "Doe", "New York", "NY", "USA", "1990-01-01");

        when(employeeService.createEmployee(any(EmployeeInputDTO.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"city\": \"New York\", \"state\": \"NY\", \"location\": \"USA\", \"birthDate\": \"1990-01-01\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeInputDTO.class));
    }

    @Test
    public void createEmployee_ShouldReturnBadRequest_WhenInputIsInvalid() throws Exception {
        // Arrange
        String invalidJson = "{\"firstName\": null, \"lastName\": null}"; // Invalid input

        // Act & Assert
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeInputDTO.class)); // Ensure service is not called
    }

    @Test
    public void updateEmployee_ShouldReturnUpdatedEmployee_WhenExists() throws Exception {
        // Arrange
        Long employeeId = 1L;
        EmployeeInputDTO inputDto = new EmployeeInputDTO("John", "Doe", "New York", "NY", "USA", "1990-01-01");
        EmployeeDetailsDTO updatedEmployee = new EmployeeDetailsDTO(employeeId, "John", "Doe", "New York", "NY", "USA", "1990-01-01");

        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeInputDTO.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"city\": \"New York\", \"state\": \"NY\", \"location\": \"USA\", \"birthDate\": \"1990-01-01\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(employeeService, times(1)).updateEmployee(eq(employeeId), any(EmployeeInputDTO.class));
    }

    @Test
    public void updateEmployee_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        // Arrange
        Long employeeId = 2L;
        EmployeeInputDTO inputDto = new EmployeeInputDTO("Jane", "Smith", "Los Angeles", "CA", "USA", "1992-02-02");

        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeInputDTO.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Jane\", \"lastName\": \"Smith\", \"city\": \"Los Angeles\", \"state\": \"CA\", \"location\": \"USA\", \"birthDate\": \"1992-02-02\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(eq(employeeId), any(EmployeeInputDTO.class));
    }

    @Test
    public void deleteEmployee_ShouldReturnNoContent_WhenEmployeeExists() throws Exception {
        // Arrange
        Long employeeId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/employees/{id}", employeeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    public void getAllEmployees_ShouldReturnPageOfEmployees_WhenMonthSpecified() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<EmployeeDetailsDTO> employees = Arrays.asList(
                new EmployeeDetailsDTO(1L, "John", "Doe", "New York", "NY", "USA", "1990-01-01")
        );
        Page<EmployeeDetailsDTO> page = new PageImpl<>(employees, pageable, employees.size());

        when(employeeService.getAllEmployeesByMonth(eq(1), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/employees")
                        .param("month", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(employeeService, times(1)).getAllEmployeesByMonth(eq(1), any(Pageable.class));
    }

    @Test
    public void getAllEmployees_ShouldUseDefaultPageSize_WhenNotSpecified() throws Exception {
        // Arrange
        Pageable defaultPageable = PageRequest.of(0, 10);
        Page<EmployeeDetailsDTO> emptyPage = new PageImpl<>(List.of(), defaultPageable, 0);

        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/employees")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(employeeService, times(1)).getAllEmployees(argThat(pageable ->
                pageable.getPageSize() == 10 && pageable.getPageNumber() == 0
        ));
    }

    @Test
    public void uploadCsvFromFileSystem_ShouldReturnSuccessMessage_WhenFileIsValid() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "John,Doe,Developer\nJane,Smith,Manager".getBytes()
        );

        doNothing().when(employeeService).processUploadedCsv(any());

        // Act & Assert
        mockMvc.perform(multipart("/employees/upload/csvFromFileSystem")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("CSV file processed and data saved successfully."));

        verify(employeeService, times(1)).processUploadedCsv(any());
    }

    @Test
    public void uploadCsvFromFileSystem_ShouldReturnErrorMessage_WhenProcessingFails() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "Invalid,CSV,Content".getBytes()
        );

        doThrow(new IOException("Error processing CSV")).when(employeeService).processUploadedCsv(any());

        // Act & Assert
        mockMvc.perform(multipart("/employees/upload/csvFromFileSystem")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing CSV file: Error processing CSV"));

        verify(employeeService, times(1)).processUploadedCsv(any());
    }

    @Test
    public void uploadCsvFromFileSystem_ShouldReturnBadRequest_WhenFileIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/employees/upload/csvFromFileSystem"))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).processUploadedCsv(any());
    }

    @Test
    public void uploadCsvFromFileSystem_ShouldReturnBadRequest_WhenFileIsEmpty() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/employees/upload/csvFromFileSystem")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).processUploadedCsv(any());
    }


    @Test
    public void uploadCsvFileFromResources_ShouldReturnSuccessMessage_WhenProcessingSucceeds() throws Exception {
        // Arrange
        doNothing().when(employeeService).saveEmployeesFromResources();

        // Act & Assert
        mockMvc.perform(post("/employees/upload/csvFromResourcesFolder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("CSV file processed and data saved successfully."));

        verify(employeeService, times(1)).saveEmployeesFromResources();
    }

    @Test
    public void uploadCsvFileFromResources_ShouldReturnErrorMessage_WhenProcessingFails() throws Exception {
        // Arrange
        doThrow(new IOException("File not found in resources")).when(employeeService).saveEmployeesFromResources();

        // Act & Assert
        mockMvc.perform(post("/employees/upload/csvFromResourcesFolder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing CSV file: File not found in resources"));

        verify(employeeService, times(1)).saveEmployeesFromResources();
    }
}

