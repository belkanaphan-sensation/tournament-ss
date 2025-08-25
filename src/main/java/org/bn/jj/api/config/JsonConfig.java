package org.bn.jj.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
        return mapper;
    }
}
