package org.bn.sensation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 1. Игнорировать неизвестные свойства
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 2. Игнорировать отсутствующие обязательные поля
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        // 3. Сериализовать только непустые поля
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 4. Красивое форматирование JSON
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // === Для Java 8 дат ===
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // === Настройка формата для ZonedDateTime ===
        SimpleModule zonedDateTimeModule = new SimpleModule();
        // Используем стандартный ISO 8601 формат для ZonedDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        zonedDateTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(formatter));
        // Десериализатор будет использовать стандартный из JavaTimeModule
        mapper.registerModule(zonedDateTimeModule);

        return mapper;
    }
}
