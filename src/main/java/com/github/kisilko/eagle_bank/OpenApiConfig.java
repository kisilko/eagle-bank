package com.github.kisilko.eagle_bank;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    public OpenAPI eagleBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eagle Bank API")
                        .description("API documentation for Eagle Bank application")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@eaglebank.com")))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
