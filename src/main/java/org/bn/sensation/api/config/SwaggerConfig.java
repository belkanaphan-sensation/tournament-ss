package org.bn.sensation.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Value("#{'${app.swagger-ui.api-version}'}")
    private String apiVersion;
    @Value("#{'${app.swagger-ui.api-title}'}")
    private String uiTitle;
    @Value("#{'${app.swagger-ui.api-description}'}")
    private String apiDescription;
    @Value("#{'${app.swagger-ui.contact-name}'}")
    private String contactName;
    @Value("#{'${app.swagger-ui.contact-email}'}")
    private String contactEmail;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList("bearer"))
                .components(new Components()
                        .addSecuritySchemes("bearer", buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .version(apiVersion)
                .title(uiTitle)
                .description(apiDescription)
                .contact(new Contact()
                        .name(contactName)
                        .email(contactEmail));
    }


    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name("bearer")
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Bearer Token")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER);
    }
}
