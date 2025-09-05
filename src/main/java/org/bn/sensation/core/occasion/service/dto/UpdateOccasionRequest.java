package org.bn.sensation.core.occasion.service.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на обновление мероприятия")
public class UpdateOccasionRequest {

    @Size(max = 255)
    @Schema(description = "Название мероприятия", example = "SBF")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание мероприятия", example = "Весенний фестиваль боевых искусств")
    private String description;

    @Schema(description = "Дата начала", example = "2025-04-13")
    private LocalDate startDate;

    @Schema(description = "Дата окончания", example = "2025-04-15")
    private LocalDate endDate;

    @Schema(description = "ID организации, проводящей мероприятие")
    private Long organizationId;
}
