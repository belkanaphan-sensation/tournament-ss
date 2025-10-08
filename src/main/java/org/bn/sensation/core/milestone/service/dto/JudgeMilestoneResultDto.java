package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;

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
@Schema(description = "Результат этапа по судьям и критериям для участника")
public class JudgeMilestoneResultDto extends BaseDto {

    @Schema(description = "Участник")
    private EntityLinkDto participant;

    @Schema(description = "Раунд")
    private EntityLinkDto round;

    @Schema(description = "Критерий, сформированный для данного этапа")
    private EntityLinkDto milestoneCriteria;

    @Schema(description = "Судья")
    private EntityLinkDto activityUser;

    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;

    @Schema(description = "Добавление участника в избранные (возможные кандидаты)", example = "true", defaultValue = "false")
    private Boolean isFavorite;
}
