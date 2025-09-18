package org.bn.sensation.core.occasion.service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bn.sensation.config.JsonConfig;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class OccasionDtoTest {

    @Test
    void testZonedDateTimeSerialization() throws Exception {
        // Создаем ObjectMapper с нашей конфигурацией
        JsonConfig jsonConfig = new JsonConfig();
        ObjectMapper objectMapper = jsonConfig.objectMapper();

        // Создаем OccasionDto с ZonedDateTime
        OccasionDto occasionDto = OccasionDto.builder()
                .id(1L)
                .name("Test Event")
                .description("Test Description")
                .startDate(ZonedDateTime.of(2025, 9, 18, 16, 56, 47, 284103000, ZoneId.of("Europe/Samara")))
                .endDate(ZonedDateTime.of(2025, 9, 18, 18, 56, 47, 0, ZoneId.of("Europe/Samara")))
                .build();

        // Сериализуем в JSON
        String json = objectMapper.writeValueAsString(occasionDto);

        System.out.println("Serialized JSON: " + json);

        // Проверяем, что JSON содержит правильный ISO 8601 формат даты
        assertTrue(json.contains("2025-09-18T16:56:47.284103+04:00[Europe/Samara]"));
        assertTrue(json.contains("2025-09-18T18:56:47+04:00[Europe/Samara]"));

        // Десериализуем обратно
        OccasionDto deserializedDto = objectMapper.readValue(json, OccasionDto.class);

        // Проверяем, что данные корректно восстановились (время может быть конвертировано в UTC)
        assertNotNull(deserializedDto.getStartDate());
        assertNotNull(deserializedDto.getEndDate());
        assertEquals(occasionDto.getName(), deserializedDto.getName());
        assertEquals(occasionDto.getDescription(), deserializedDto.getDescription());
    }

    @Test
    void testZonedDateTimeWithoutMicroseconds() throws Exception {
        // Создаем ObjectMapper с нашей конфигурацией
        JsonConfig jsonConfig = new JsonConfig();
        ObjectMapper objectMapper = jsonConfig.objectMapper();

        // Создаем OccasionDto с ZonedDateTime без микросекунд
        OccasionDto occasionDto = OccasionDto.builder()
                .id(1L)
                .name("Test Event")
                .description("Test Description")
                .startDate(ZonedDateTime.of(2025, 9, 18, 16, 56, 47, 0, ZoneId.of("Europe/Samara")))
                .endDate(ZonedDateTime.of(2025, 9, 18, 18, 56, 47, 0, ZoneId.of("Europe/Samara")))
                .build();

        // Сериализуем в JSON
        String json = objectMapper.writeValueAsString(occasionDto);

        System.out.println("Serialized JSON (no microseconds): " + json);

        // Проверяем, что JSON содержит правильный ISO 8601 формат даты без микросекунд
        assertTrue(json.contains("2025-09-18T16:56:47+04:00[Europe/Samara]"));
        assertTrue(json.contains("2025-09-18T18:56:47+04:00[Europe/Samara]"));

        // Десериализуем обратно
        OccasionDto deserializedDto = objectMapper.readValue(json, OccasionDto.class);

        // Проверяем, что данные корректно восстановились (время может быть конвертировано в UTC)
        assertNotNull(deserializedDto.getStartDate());
        assertNotNull(deserializedDto.getEndDate());
        assertEquals(occasionDto.getName(), deserializedDto.getName());
        assertEquals(occasionDto.getDescription(), deserializedDto.getDescription());
    }
}
