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


    public List<EmployeeInputDTO> loadEmployeesFromCsv(Resource resource) throws IOException {
        List<EmployeeInputDTO> employees = new ArrayList<>();

        // Open the CSV file for reading
        try (BufferedReader reader = createReader(resource)) {
            // Using CSVFormat.Builder to specify that the first row should be treated as headers
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()  // Automatically treat the first row as headers
                    .setSkipHeaderRecord(true)  // Skip the header row
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(reader);

            for (CSVRecord record : records) {
                // Extract fields from the CSV record
                String firstName = record.get("First name");
                String lastName = record.get("Last name");
                String location = record.get("Location");
                // Split the location into city and state
                String[] locationParts = location.split(",", 2); // Split at the first comma
                String city = locationParts[0].trim();  // City is the first part
                String state = locationParts.length > 1 ? locationParts[1].trim() : "";  // State is the second part

                String birthdayStr = record.get("Birthday");

                // Create and add the Employee object to the list
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

