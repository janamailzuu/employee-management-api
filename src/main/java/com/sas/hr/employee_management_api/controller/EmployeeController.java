package com.sas.hr.employee_management_api.controller;


import com.sas.hr.employee_management_api.dto.EmployeeDetailsDTO;
import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import com.sas.hr.employee_management_api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/employees")
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Upload CSV file from resources folder",
            description = "Processes and saves employee data from a CSV file located in the resources folder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CSV file uploaded and processed successfully.",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Error uploading file.",
                    content = @Content)
    })
    @PostMapping("/import-from-resources")
    public ResponseEntity<String> importCsvFromResources(){
        try {
            employeeService.saveEmployeesFromResources();
            return ResponseEntity.status(HttpStatus.CREATED).body("CSV file processed and data saved successfully.");
        } catch (IOException e) {
            log.error("Error occured in file upload:: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing CSV file: " + e.getMessage());
        }
    }

    @Operation(summary = "Upload CSV file from file system",
            description = "Processes and saves employee data from a CSV file uploaded from the file system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CSV file processed and data saved successfully.",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "File is empty.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Error processing CSV file.",
                    content = @Content)
    })
    @PostMapping("/upload-from-file")
    public ResponseEntity<String> uploadCsvFromFileSystem(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        try {
            employeeService.processUploadedCsv(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("CSV file processed and data saved successfully.");
        } catch (IOException e) {
            log.error("Error occured in csv file processing :: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing CSV file: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all employees", description = "Retrieves a paginated list of all employees, with optional filtering by month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of employees",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid page, size, sortBy, or month parameter",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDetailsDTO>> getAllEmployees(@RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) int page,
                                                                    @RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) @Max(100) int size,
                                                                     @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
                                                                    @Parameter(description = "Optional query to filter the employee list by month")
                                                                        @RequestParam(value = "month", required = false) @Min(1) @Max(12) Integer month) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<EmployeeDetailsDTO> employees;
        if (month != null) {
            employees = employeeService.getAllEmployeesByMonth(month,pageable);
        } else {
            employees = employeeService.getAllEmployees(pageable);
        }
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Create a new employee", description = "Creates a new employee record based on the provided input data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeDetailsDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmployeeDetailsDTO> createEmployee(    @Parameter(description = "Employee object to create", required = true)
                                                                     @RequestBody @Validated EmployeeInputDTO employeeInputDTO) {
       EmployeeDetailsDTO employeeDetailsDTO = employeeService.createEmployee(employeeInputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeDetailsDTO);
    }

    @Operation(summary = "Update an employee", description = "Updates an existing employee's details based on the provided ID and input data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee successfully updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeDetailsDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied or invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDetailsDTO> updateEmployee(@Parameter(description = "The ID of the employee to fetch", required = true) @PathVariable @NotNull(message = "Employee ID must not be null")
                                                                 @Min(value = 1, message = "Employee ID must be greater than or equal to {value}")
                                                                 @Max(value = 999999999999999L, message = "Employee ID must be less than or equal to {value}") Long id, @RequestBody @Valid EmployeeInputDTO employeeInputDTO) {
        EmployeeDetailsDTO employeeDetailsDTO =  employeeService.updateEmployee(id, employeeInputDTO);
        return employeeDetailsDTO != null ? ResponseEntity.ok(employeeDetailsDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get an employee by ID", description = "Retrieves an employee's details based on the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeDetailsDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailsDTO> getEmployeeById(@Parameter(description = "The ID of the employee to fetch", required = true, example = "1")
                                                                      @PathVariable @NotNull(message = "Employee ID must not be null")
                                                                  @Min(value = 1, message = "Employee ID must be greater than or equal to {value}")
                                                                  @Max(value = 999999999999999L, message = "Employee ID must be less than or equal to {value}") Long id) {
        EmployeeDetailsDTO employeeDetailsDTO = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employeeDetailsDTO);
    }

    @Operation(summary = "Delete an employee", description = "Deletes an employee record based on the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@Parameter(description = "The ID of the employee to delete", required = true, example = "1") @PathVariable @NotNull(message = "Employee ID must not be null")
                                                   @Min(value = 1, message = "Employee ID must be greater than or equal to {value}")
                                                   @Max(value = 999999999999999L, message = "Employee ID must be less than or equal to {value}")  @Pattern(regexp = "\\d+", message = "ID must be numeric") @Min(1) Long id) {
             employeeService.deleteEmployeeById(id);
             return ResponseEntity.noContent().build();
    }

}