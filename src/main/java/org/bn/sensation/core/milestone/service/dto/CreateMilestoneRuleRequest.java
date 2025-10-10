package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.milestone.entity.AssessmentMode;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Запрос на создание правила этапа")
public class CreateMilestoneRuleRequest extends EmptyDto {

    @NotNull
    @Schema(description = "Режим оценивания участника", example = "PASS")
    private AssessmentMode assessmentMode;

    @NotNull
    @Positive
    @Schema(description = "Максимальное количество участников в этапе", example = "10")
    private Integer participantLimit;

    @NotNull
    @Schema(description = "ID этапа, к которому относится правило", example = "1")
    private Long milestoneId;
}
