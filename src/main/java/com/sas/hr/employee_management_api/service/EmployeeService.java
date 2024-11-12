package com.sas.hr.employee_management_api.service;


import com.sas.hr.employee_management_api.dto.EmployeeDetailsDTO;
import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import com.sas.hr.employee_management_api.exception.EmployeeNotFoundException;
import com.sas.hr.employee_management_api.mapper.EmployeeMapper;
import com.sas.hr.employee_management_api.model.Employee;
import com.sas.hr.employee_management_api.repository.EmployeeJpaRepository;
import com.sas.hr.employee_management_api.repository.EmployeeRepository;
import com.sas.hr.employee_management_api.util.CSVProcessor;
import com.sas.hr.employee_management_api.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final CSVProcessor csvProcessor;
    private final EmployeeJpaRepository employeeJpaRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CSVProcessor csvProcessor, EmployeeJpaRepository employeeJpaRepository) {
        this.employeeRepository = employeeRepository;
        this.csvProcessor = csvProcessor;
        this.employeeJpaRepository = employeeJpaRepository;
    }

    /**
     * Processes a CSV file to load employee data and persist it to the database.
     * This method reads employee information from the specified CSV resource, converts
     * the data into a list of {@link EmployeeInputDTO} objects, maps these DTOs to
     * {@link Employee} entities, and then persists the entities to the database.
     *
     * @param resource The {@link Resource} representing the CSV file to be processed.
     * @throws IOException if an error occurs while reading the CSV file or processing its contents.
     */
    private void processCsvFile(Resource resource) throws IOException {
        List<EmployeeInputDTO> employeeDTOList = loadEmployeesFromCsv(resource);
        List<Employee> employeeList = EmployeeMapper.toEmployeeEntityList(employeeDTOList);
        persistEmployees(employeeList);
    }

    /**
     * Loads employee data from a CSV file and returns a list of EmployeeInputDTO objects.
     *
     * This method reads employee information from the provided CSV resource and converts
     * it into a list of {@link EmployeeInputDTO} objects. It utilizes a CSV processor
     * to handle the parsing and conversion of the CSV data.
     *
     * @param resource The {@link Resource} representing the CSV file to be processed.
     * @return A list of {@link EmployeeInputDTO} objects containing the employee data loaded from the CSV.
     * @throws IOException if an error occurs while reading the CSV file or processing its contents.
     */
    private List<EmployeeInputDTO> loadEmployeesFromCsv(Resource resource) throws IOException {
        return csvProcessor.loadEmployeesFromCsv(resource);
    }

    /**
     * Persists a list of employee records to the database.
     *
     * This method takes a list of {@link Employee} objects and saves them to the database
     * using a batch insert operation. This approach is more efficient than saving each
     * employee individually, especially when dealing with large datasets.
     *
     * @param employeeList A list of {@link Employee} objects to be persisted in the database.
     *                     Must not be null or empty.
     */
    private void persistEmployees(List<Employee> employeeList) {
        employeeRepository.batchInsertEmployeesUsingJdbc(employeeList);
    }

    /**
     * Processes an uploaded CSV file by saving it to a temporary location and then processing its contents.
     *
     * This method accepts a {@link MultipartFile} as input, saves it to a temporary file,
     * and then processes the CSV file using a designated method. The temporary file is created
     * to avoid conflicts with existing files and to ensure that the uploaded data can be handled
     * safely without affecting the original file.
     *
     * @param file The CSV file uploaded by the user, represented as a {@link MultipartFile}.
     * @throws IOException if an error occurs while saving the file or processing its contents.
     */
    public void processUploadedCsv(MultipartFile file) throws IOException {
        Path tempFilePath = saveMultipartFileToTemp(file);
        Resource resource = new FileSystemResource(tempFilePath.toFile());
        processCsvFile(resource);
    }

    /**
     * Saves a {@link MultipartFile} to a temporary directory and returns the path of the saved file.
     *
     * This method generates a unique filename to avoid name collisions, creates a temporary
     * directory, and saves the uploaded file within that directory. The method ensures that
     * any existing file with the same name is replaced. The path to the saved temporary file
     * is returned for further processing.
     *
     * @param file The MultipartFile to be saved as a temporary file.
     * @return The {@link Path} of the saved temporary file.
     * @throws IOException if an error occurs while creating the temporary directory or copying the file.
     */
    private Path saveMultipartFileToTemp(MultipartFile file) throws IOException {
        String tempFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path tempDir = Files.createTempDirectory("uploadedFiles");
        Path tempFilePath = tempDir.resolve(tempFileName);
        Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

    /**
     * Saves employee records from a CSV file located in the classpath.
     *
     * This method reads employee data from a CSV file specified by the resource path
     * and processes it to save the employee records into the database. The path to the
     * CSV file is defined as a classpath resource. If an error occurs while reading
     * the file, an {@link IOException} will be thrown.
     *
     * @throws IOException if an error occurs while reading the CSV file.
     */    public void saveEmployeesFromResources() throws IOException {
        Resource resource = new ClassPathResource("static/data/ProgrammingChallengeData.csv"); // adjust based on your structure
        processCsvFile(resource);
    }

    /**
     * Retrieves a paginated list of all employees.
     *
     * This method queries the repository for all employees and returns the results
     * as a paginated {@link Page} of {@link EmployeeDetailsDTO}. The pagination
     * is controlled by the provided {@link Pageable} parameter.
     *
     * @param pageable The pagination information including page number and size.
     * @return A {@link Page} containing {@link EmployeeDetailsDTO} objects representing
     *         all employees in the database.
     */
    public Page<EmployeeDetailsDTO> getAllEmployees(Pageable pageable){
        Page<Employee> employeeList = employeeJpaRepository.findAll(pageable);
        return EmployeeMapper.convertPageEmployeeToDTO(employeeList);
    }

    /**
     * Retrieves a paginated list of employees whose birthdays fall in the specified month.
     *
     * This method queries the repository for employees with birthdays in the given month
     * and returns the results as a paginated {@link Page} of {@link EmployeeDetailsDTO}.
     *
     * @param month The month (1-12) for which to retrieve employees' birthday information.
     *              Must be a valid month number.
     * @param pageable The pagination information including page number and size.
     * @return A {@link Page} containing {@link EmployeeDetailsDTO} objects representing
     *         the employees whose birthdays are in the specified month.
     */
    public Page<EmployeeDetailsDTO> getAllEmployeesByMonth(int month,Pageable pageable) {
        Page<Employee> employeeList =  employeeRepository.findEmployeesByBirthdayMonth(month,pageable);
        return EmployeeMapper.convertPageEmployeeToDTO(employeeList);
    }

    /**
     * Deletes an employee record from the database based on the provided ID.
     * 
     * This method retrieves the employee with the specified ID from the repository.
     * If the employee is found, it will be deleted. If no employee with the given ID
     * exists, an {@link EmployeeNotFoundException} will be thrown.
     *
     * @param id The ID of the employee to delete. Must be a valid, existing employee ID.
     * @throws EmployeeNotFoundException if no employee with the specified ID exists in the database.
     */
    public void deleteEmployeeById(Long id) {
        Employee employee = employeeJpaRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        employeeJpaRepository.delete(employee);
    }


    /**
     * Updates an existing employee record with the provided details.
     *
     * This method retrieves the employee with the specified ID from the repository.
     * If the employee is found, it updates the employee's details based on the
     * information provided in the {@link EmployeeInputDTO}. The updated employee
     * record is then saved back to the repository. If no employee with the given ID
     * exists, an {@link EmployeeNotFoundException} will be thrown.
     *
     * @param id The ID of the employee to update. Must be a valid, existing employee ID.
     * @param employeeInputDTO The DTO containing the new details for the employee.
     *                         It should include fields such as first name, last name,
     *                         location, and birth date.
     * @return An {@link EmployeeDetailsDTO} representing the updated employee.
     * @throws EmployeeNotFoundException if no employee with the specified ID exists in the database.
     */
    public EmployeeDetailsDTO updateEmployee(Long id, EmployeeInputDTO employeeInputDTO) {

        Employee employee = employeeJpaRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setLocation(employeeInputDTO.location());
        employee.setBirthDay(DateUtil.convertDateStringToFormattedLocalDate(employeeInputDTO.birthDate()));
        employee.setFirstName(employeeInputDTO.firstName());
        employee.setLastName(employeeInputDTO.lastName());
        Employee resultEmployee = employeeJpaRepository.save(employee);
        return EmployeeMapper.toEmployeeDTO(resultEmployee);
    }

    /**
     * Creates a new employee record in the database using the provided details.
     * This method maps the provided {@link EmployeeInputDTO} to an {@link Employee} entity
     * and saves it to the repository. Upon successful creation, it returns the details of
     * the newly created employee as an {@link EmployeeDetailsDTO}.
     *
     * @param employeeInputDTO The DTO containing the details for the new employee.
     *                         It should include fields such as first name, last name,
     *                         location, and birth date.
     * @return An {@link EmployeeDetailsDTO} representing the newly created employee.
     */    public EmployeeDetailsDTO createEmployee(EmployeeInputDTO employeeInputDTO) {
        Employee employee = EmployeeMapper.toEmployeeEntity(employeeInputDTO);
        Employee resultEmployee = employeeJpaRepository.save(employee);
        return EmployeeMapper.toEmployeeDTO(resultEmployee);
    }

    /**
     * Retrieves an employee's details based on the provided ID.
     *
     * This method searches for an employee in the database using the specified ID.
     * If the employee is found, it converts the employee entity to an {@link EmployeeDetailsDTO}
     * and returns it. If no employee with the given ID exists, an {@link EmployeeNotFoundException}
     * will be thrown.
     *
     * @param id The ID of the employee to retrieve. Must be a valid, existing employee ID.
     * @return An {@link EmployeeDetailsDTO} representing the details of the found employee.
     * @throws EmployeeNotFoundException if no employee with the specified ID exists in the database.
     */
    public EmployeeDetailsDTO getEmployeeById(Long id) {
        Optional<Employee> employee = employeeJpaRepository.findById(id);
        return employee.map(EmployeeMapper::toEmployeeDTO)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

}
