package com.joserojas.supportdesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI supportDeskOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SupportDesk API")
                        .version("v1")
                        .description("Backend REST API for support desk ticket management."));
    }
}
