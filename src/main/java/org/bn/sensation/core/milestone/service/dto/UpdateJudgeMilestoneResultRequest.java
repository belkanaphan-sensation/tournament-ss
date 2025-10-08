package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class UpdateJudgeMilestoneResultRequest extends EmptyDto {

    @PositiveOrZero
    @NotNull
    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;

    @Schema(description = "Добавление участника в избранные (возможные кандидаты)", example = "true", defaultValue = "false")
    private Boolean isFavorite;
}
