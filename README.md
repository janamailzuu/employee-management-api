# Employee Management API

## Overview
This project provides a RESTful API for managing employee data.It allows for CRUD operations (Create, Read, Update, Delete) on employee records and  supports uploading employee data through CSV files, both from the resources folder and local file system.

## Features
- **CRUD Operations**: Create, Read, Update, Delete employee records.
- **Birthday Filter**: Retrieve employees based on their birthday month.
- **Import or Upload CSV Data**: Import the CSV from the resources folder or upload it from local filesystem.
- **RESTful API**: Interacts with JSON requests and responses.
- **In-memory H2 Database**: Stores employee information (easily replaceable with other databases).

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

