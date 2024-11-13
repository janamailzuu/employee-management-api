# Employee Management API

## Overview
This project provides a RESTful API for managing employee data.It allows for CRUD operations (Create, Read, Update, Delete) on employee records and  supports uploading employee data through CSV files, both from the resources folder and local file system.

## Features
- **CRUD Operations**: Create, Read, Update, Delete employee records.
- **Birthday Filter**: Retrieve employees based on their birthday month.
- **Import or Upload CSV Data**: Import the CSV from the resources folder or upload it from local filesystem.
- **RESTful API**: Interacts with JSON requests and responses.
- **In-memory H2 Database**: Stores employee information (easily replaceable with other databases).

# CSV File Upload Strategy

## Overview
When designing the CSV file upload functionality, several strategies were considered to balance ease of use, performance, and scalability. Below are the main approaches evaluated along with their pros and cons:

---

### 1. Direct File Upload via HTTP (Current Implementation)

**Approach**: The user uploads the CSV file via an HTTP POST request. The file can either be uploaded from the filesystem or imported from the resources folder.

#### Pros:
- Simple to implement.
- Low overhead for small to medium-sized files.
- Flexible (supports both local file system and resources folder).

#### Cons:
- Synchronous processing, which can block the application for large files.
- Limited scalability for handling large datasets efficiently.

**Summary**: This approach was chosen for its simplicity and flexibility. The ability to handle file uploads from both the filesystem and resources folder makes it versatile. However, for large files, it may become a bottleneck.

---

### 2. Batch Insertion for Database Upload (Current Implementation)

**Approach**: Instead of inserting each record from the CSV file one by one, the records are processed in bulk using JDBC batch insertion. This minimizes database round trips and improves performance.

#### Pros:
- Improved performance by reducing the number of database round trips.
- Reduces database load with bulk insertion, especially for large datasets.

#### Cons:
- Increased memory usage when processing large files.
- Error handling can be more complex, as issues in one batch might affect others.

**Summary**: The batch insertion approach was selected to improve performance, especially when handling large files. By reducing the number of database queries, it enhances efficiency while ensuring data is uploaded in bulk.

---

### 3. Combination of File Upload and Batch Insertion

**Approach**: The solution combines both **Direct File Upload** and **Batch Insertion**. The user uploads the CSV file (either from the filesystem or the resources folder), and the data is read and inserted into the database using **JDBC batch processing**.

#### Pros:
- **Best of both worlds**: Leverages the simplicity of direct file upload while improving database performance with batch insertion.
- **Scalable for moderate file sizes**: This approach is efficient enough for small to medium-sized files, yet performs well for larger files thanks to batch processing.
- Flexible file upload mechanism (local file system or resources folder).

#### Cons:
- **Blocking operation**: While batch inserts optimize database performance, the processing is still synchronous and may cause delays for very large files.
- **Memory usage**: For larger files, the entire dataset is loaded into memory before performing the batch update, which could lead to high memory consumption.

**Summary**: This approach balances simplicity with performance. Direct file uploads allow for easy handling of files, while JDBC batch updates optimize database operations. This combination is suitable for most file sizes in this use case, but scalability for very large files could require further optimizations like asynchronous processing or streaming.

---

### 4. Asynchronous File Processing (Future Enhancement)

**Approach**: The file is uploaded via HTTP, but the processing happens asynchronously in the background (using `@Async`, Spring Batch, or a similar solution).

#### Pros:
- Non-blocking: allows the user to continue using the app while the file is being processed.
- Better performance for large files.
- Scalable for high-volume uploads.

#### Cons:
- Adds complexity with background job management.
- Requires additional infrastructure for job tracking and status updates.

**Summary**: Asynchronous processing can be added as a future enhancement for large files, improving scalability and user experience.

---

### 5. Cloud Storage Upload (SFTP / Cloud Integration)

**Approach**: Files are uploaded to cloud storage (e.g., Amazon S3, Google Cloud Storage) or via SFTP, and then processed from the cloud storage.

#### Pros:
- Scalable and reliable for large files.
- Secure transfer options (SFTP).
- Cloud storage provides durability and accessibility.

#### Cons:
- Additional complexity in setting up and managing cloud storage integration.
- Possible additional costs for cloud storage services.

**Summary**: Cloud-based uploads are ideal for large-scale applications but require additional setup and may incur costs.

---

### 6. Pre-Processing and Validation of CSV File

**Approach**: Before importing the CSV data into the database, the file undergoes validation (e.g., checking for missing fields, duplicates, or invalid data).

#### Pros:
- Ensures data integrity and prevents invalid records from being uploaded.
- Improves data quality by enforcing business rules during validation.

#### Cons:
- Adds processing time and complexity.
- Requires additional logic to handle all validation cases.

**Summary**: Pre-processing and validation ensure data quality but can increase the overall upload time, especially for large files.

---

## Chosen Approach

After evaluating the options, the **combination of Direct File Upload and Batch Insertion** was selected. This approach allows for:

- **File Upload Flexibility**: Files can be uploaded either from the local filesystem or the resources folder.
- **Efficient Database Operations**: Using **JDBC batch insertion** reduces database round trips, improving performance when handling large datasets.

While this solution is sufficient for most use cases, **future enhancements** could include **asynchronous processing** for large files and **cloud storage integration** for better scalability and fault tolerance.

---

## Conclusion

This section outlines the strategies considered for implementing CSV file uploads. The chosen solution strikes a balance between simplicity (via direct file upload) and performance (via batch inserts). Future improvements could focus on **asynchronous processing** for scalability and **cloud-based solutions** for greater reliability and performance.
















## Technologies Used
- **Java 21**
- **Spring Boot** (3.3.5)
- **Spring Data JPA**: Manages the persistence layer.
- **H2 Database**: Lightweight, in-memory database for development.
- **Apache Commons CSV**: For CSV file parsing
- **Maven**: Dependency and build management.
-  **Spring Boot Starter Actuator**: To expose useful endpoints for monitoring the application.
- **Spring Boot Starter Test**: For unit and integration testing.

## Prerequisites
- **Java 21**
- **Maven** installed
- **Git** (if cloning from a repository)

## Setup Instructions
1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd employee-management-api
    ```
2. **Build the project:**
```bash
mvn clean install
```

3. **Run the Application Use Maven to start the Spring Boot application:**
```bash
mvn spring-boot:run
```

Alternatively, you can package the application as a JAR and run it:

 ```bash
mvn clean package
java -jar target/employee-management-api-0.0.1-SNAPSHOT.jar
```

4. **Access the application at:**
   - `http://localhost:8080`

### API Endpoints

| Method | Endpoint | Description |
|-----------------|-----------------|-----------------|
| POST | /api/employees | Create a new employee |
| GET | /api/employees | Get all employees |
| GET | /api/employees/{id} | Get an employee by ID |
| PUT | /api/employees/{id} | Update an employee by ID |
| DELETE | /api/employees/{id} | Delete an employee by ID |
| GET | /api/employees?month={month} | Get employees with birthdays in a given month |
| POST | /api/employees/import-from-resources | Upload CSV file from resources folder |
| POST | /api/employees/upload-from-file | Upload CSV file from file system |


### Example Requests

Create a New Employee

 ```http
POST /api/employees
Content-Type: application/json

{
  "First name": "Luisa",
  "Last name": "Brakus",
  "Location": "San Diego, CA"
  "Birthday": "1/30/2001"
}
 ```

Get Employees with July Birthdays

```http
GET /api/employees?month=7
 ```


## Data Model

The Employee entity includes:

- id: Unique identifier (auto-generated).
- first_name: Employee’s first name.
- last_name: Employee’s last name.
- location: Location of the employee.
- city: City
- state: state
- birth_day: birthdate of the employee

## Testing Strategy

- Unit Testing: Tests individual methods using JUnit and Mockito.
- Integration Testing: Validates API endpoints and JPA operations with in-memory H2 database.
- Postman Collection: [Optional] Use Postman to verify the API responses.


## Example Usage with cURL

Add an Employee

```bash
curl -X POST http://localhost:8080/api/employees -H "Content-Type: application/json" -d '{
    "First Name": "Luisa",
    "Last Name": "Brakus",
    "Location": "San Diego, CA",
    "birthday": "05/10/2001"
}'
```

Get Employees by Birthday Month

```bash
curl http://localhost:8080/api/employees?month=5
```

## Running Tests

### Unit Tests
Unit tests are written using JUnit 5 and Spring Boot Test. You can run the tests using Maven:
```bash
mvn test
```

### Integration Tests
The integration tests ensure that the API is working as expected by sending HTTP requests to the endpoints and checking responses.


## Error Handling
- **400 Bad Request**: The request is malformed, or required data is missing.
- **404 Not Found**: The requested resource (e.g., employee) does not exist.
- **500 Internal Server Error**: An unexpected error occurred on the server.

## Monitoring and Health Check

This application uses Spring Boot Actuator for monitoring and health checks. The following endpoints are available:

- Health check: `http://localhost:8080/api/actuator/health`
- Metrics: `http://localhost:8080/api/actuator/metrics`
- Info: `http://localhost:8080/api/actuator/info`


## Possible Enhancements

- Asynchronous processing for handling large files.
- Validation: Add more robust validation for fields such as birthday.
- Creating error report after the CSV processing
- File Validation and Pre-processing - 415 Unsupported Media Type for the file type validation, correctness of the content, avoiding duplicates
-  API versioning
-  Authentication and Authorization for role based access
-  Frontend Integration: Build a simple frontend for HR to manage employee data and upload files via a user-friendly interface.
-  Database Support: Switch from H2 to MySQL, MongoDB, PostgreSQL, etc.
-  Switch to use testContainers for Integration tests while switch database from h2.