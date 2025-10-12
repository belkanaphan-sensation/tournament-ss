package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.milestone.entity.AssessmentMode;

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
@Schema(description = "Правило этапа")
public class MilestoneRuleDto extends BaseDto {

    @Schema(description = "Режим оценивания участника", example = "PASS")
    private AssessmentMode assessmentMode;

    @Schema(description = "Максимальное количество участников в этапе", example = "10")
    private Integer participantLimit;

    @Schema(description = "Максимальное количество участников в раунде этапа. Не больше чем participantLimit", example = "10")
    private Integer roundParticipantLimit;

    @Schema(description = "Нужно ли строго соблюдать количество пропущенных в следующий этап участников", example = "false")
    private Boolean strictPassMode;

    @Schema(description = "Этап, к которому относится правило")
    private EntityLinkDto milestone;
}
