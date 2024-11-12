package com.sas.hr.employee_management_api.util;

import com.sas.hr.employee_management_api.dto.EmployeeInputDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CSVProcessor {


    /**
     * Loads employee data from a CSV file and converts it into a list of {@link EmployeeInputDTO} objects.
     * The method reads the CSV file, processes each row, and splits the location field into city and state.
     * It assumes the CSV file contains a header row, which will be skipped during parsing. The method
     * extracts the first name, last name, location, and birthday for each employee and creates an
     * {@link EmployeeInputDTO} object, which is then added to the result list.
     *
     * @param resource The resource representing the CSV file to be loaded. This can be a file, classpath resource, etc.
     * @return A list of {@link EmployeeInputDTO} objects containing the employee data loaded from the CSV file.
     * @throws IOException If an I/O error occurs while reading the CSV file.
     */
    public List<EmployeeInputDTO> loadEmployeesFromCsv(Resource resource) throws IOException {
        List<EmployeeInputDTO> employees = new ArrayList<>();

        try (BufferedReader reader = createReader(resource)) {
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(reader);

            for (CSVRecord record : records) {
                String firstName = record.get("First name");
                String lastName = record.get("Last name");
                String location = record.get("Location");

                String[] locationParts = location.split(",", 2);
                String city = locationParts[0].trim();
                String state = locationParts.length > 1 ? locationParts[1].trim() : "";

                String birthdayStr = record.get("Birthday");


                EmployeeInputDTO employee = new EmployeeInputDTO(firstName, lastName,city,state, location, birthdayStr);
                employees.add(employee);
            }
        }

        return employees;
    }


    /**
     * Creates a BufferedReader to read the resource based on its type (classpath or filesystem).
     * @param resource The resource to read from.
     * @return A BufferedReader for the resource.
     * @throws IOException If there is an issue opening the resource.
     */
    private BufferedReader createReader(Resource resource) throws IOException {
        // If resource is from the classpath, use InputStreamReader
        if (resource.isFile()) {
            // If it's a file system resource, we can use FileReader
            return new BufferedReader(new FileReader(resource.getFile()));
        } else {
            // If it's a classpath resource (e.g., JAR), use InputStreamReader
            return new BufferedReader(new InputStreamReader(resource.getInputStream()));
        }
    }
}

