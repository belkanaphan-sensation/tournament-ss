package org.bn.sensation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", cookieSecurityScheme()));
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

    private SecurityScheme cookieSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("Session cookie");
    }
}
