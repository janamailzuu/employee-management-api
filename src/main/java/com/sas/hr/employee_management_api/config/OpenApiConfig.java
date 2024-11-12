package com.sas.hr.employee_management_api.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Management API",
                version = "1.0.0",
                description = "This API allows users to manage employee information."
        )
)
public class OpenApiConfig {
}
