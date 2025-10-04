package org.bn.sensation.core.milestone.service.dto;

import java.util.List;

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
@Schema(description = "Результат этапа для участника")
public class MilestoneResultDto extends BaseDto {

    @Schema(description = "Участник")
    private EntityLinkDto participant;

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Прошел участник в следующий этап или нет")
    private Boolean passed;

    @Schema(description = "Суммарный балл этапа для участника", example = "5")
    private Integer totalScore;

    @Schema(description = "Результаты по критериям")
    private List<MilestoneCriteriaScoreDto> criteriaScores;

}
