package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.milestone.entity.PassStatus;

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

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Участник")
    private EntityLinkDto participant;

    @Schema(description = "Прошел участник в следующий этап или нет по результатам оценок")
    private PassStatus judgePassed;

    @Schema(description = "Прошел участник в следующий этап или нет по решению организатора")
    private Boolean finallyApproved;

    @Schema(description = "Суммарный балл этапа для участника", example = "5")
    private Integer totalScore;

}
