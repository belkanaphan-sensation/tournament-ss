package org.bn.sensation.core.judge.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class JudgeMilestoneResultMilestoneRequest extends BaseDto {

    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;

    @Schema(description = "Добавление участника в избранные (возможные кандидаты)", example = "true", defaultValue = "false")
    private Boolean isFavorite;

    @Schema(description = "Участник - возможный кандидат для выбора в этапе", example = "true", defaultValue = "false")
    private Boolean isCandidate;
}
