package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.State;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на обновление вехи")
public class UpdateMilestoneRequest extends EmptyDto {

    @Size(max = 255)
    @Schema(description = "Название этапа", example = "Квалификация")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание этапа", example = "Квалификационный этап соревнования")
    private String description;

    @Schema(description = "Статус этапа", example = "DRAFT")
    private State state;

    @PositiveOrZero
    @Schema(description = "Порядок этапа в рамках активности", example = "1")
    private Integer milestoneOrder;
}
