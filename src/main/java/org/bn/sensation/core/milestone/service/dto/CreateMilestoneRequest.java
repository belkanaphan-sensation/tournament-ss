package org.bn.sensation.core.milestone.service.dto;

import java.util.List;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на создание этапа")
public class CreateMilestoneRequest extends EmptyDto {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название этапа", example = "Квалификация")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание этапа", example = "Квалификационный этап соревнования")
    private String description;

    @NotNull
    @Schema(description = "ID активности, частью которой является этап")
    private Long activityId;

    @PositiveOrZero
    @Schema(description = "Порядок этапа в рамках активности (если не указан, будет рассчитан автоматически)", example = "1")
    private Integer milestoneOrder;

    @Schema(description = "Список ID участников для этапа. Доступно только для суперадмина")
    private List<Long> participantIds;
}
