package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.milestone.entity.AssessmentMode;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Запрос на обновление правила этапа")
public class UpdateMilestoneRuleRequest extends EmptyDto {

    @Schema(description = "Режим оценивания участника", example = "PASS")
    private AssessmentMode assessmentMode;

    @Positive
    @Schema(description = "Максимальное количество участников в этапе", example = "10")
    private Integer participantLimit;

    @Positive
    @Schema(description = "Максимальное количество участников в раунде этапа. Не больше чем participantLimit", example = "10")
    private Integer roundParticipantLimit;

//    @Schema(description = "Нужно ли строго соблюдать количество пропущенных в следующий этап участников", example = "false")
//    private Boolean strictPassMode;
}
