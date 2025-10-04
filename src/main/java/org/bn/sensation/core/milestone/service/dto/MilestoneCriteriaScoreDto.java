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
@Schema(description = "Суммарный балл участника по критерию в рамках этапа")
public class MilestoneCriteriaScoreDto extends BaseDto {

    @Schema(description = "Назначение критерия на этап")
    private EntityLinkDto milestoneCriteriaAssignment;

    @Schema(description = "Суммарный балл по критерию", example = "85")
    private Integer totalScore;
}
