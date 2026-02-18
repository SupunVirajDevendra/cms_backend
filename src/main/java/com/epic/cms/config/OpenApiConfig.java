package com.epic.cms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cardManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Management System API")
                        .description("REST API for managing credit cards and processing card requests")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Epic Lanka")
                                .email("support@epic.lanka")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }
}
