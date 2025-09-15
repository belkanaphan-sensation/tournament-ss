package org.bn.sensation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// чтобы не ругался при логине
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Добавляем обработчик для favicon.ico
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // Добавляем обработчик для Chrome DevTools
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/")
                .setCachePeriod(3600);

        // Добавляем обработчик для пустых запросов (точка)
        registry.addResourceHandler("/.")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
