package org.bn.sensation.core.milestone.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на создание вехи")
public class CreateMilestoneRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название вехи", example = "Квалификация")
    private String name;

    @Schema(description = "ID активности, частью которой является веха")
    private Long activityId;
}
