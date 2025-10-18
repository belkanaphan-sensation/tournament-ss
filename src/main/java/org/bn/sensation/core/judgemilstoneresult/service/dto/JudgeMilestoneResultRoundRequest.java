package org.bn.sensation.core.judgemilstoneresult.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на создание результатов судьи для раунда в этапе")
public class JudgeMilestoneResultRoundRequest extends BaseDto {

    @Schema(description = "Участник. При апдейте игнорируется, т.к. не может быть изменен")
    private Long participantId;

    @Schema(description = "Раунд. При апдейте игнорируется, т.к. не может быть изменен")
    private Long roundId;

    @Schema(description = "Критерий, сформированный для данного этапа. При апдейте игнорируется, т.к. не может быть изменен")
    private Long milestoneCriteriaId;

    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;

    @Schema(description = "Добавление участника в избранные (возможные кандидаты)", example = "true", defaultValue = "false")
    private Boolean isFavorite;

    @Schema(description = "Участник - возможный кандидат для выбора в этапе", example = "true", defaultValue = "false")
    private Boolean isCandidate;
}
