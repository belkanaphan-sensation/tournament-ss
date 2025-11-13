package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.contestant.entity.ContestantType;
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
    @Schema(description = "Режим оценивания конкурсанта", example = "PASS")
    private AssessmentMode assessmentMode;

    @NotNull
    @Schema(description = "Тип конкурсанта", example = "SINGLE")
    private ContestantType contestantType;

    @NotNull
    @Positive
    @Schema(description = "Максимальное количество конкурсантов в этапе", example = "10")
    private Integer contestantLimit;

    @NotNull
    @Positive
    @Schema(description = "Максимальное количество конкурсантов в раунде этапа. Не больше чем contestantLimit", example = "10")
    private Integer roundContestantLimit;

    @NotNull
    @Schema(description = "ID этапа, к которому относится правило", example = "1")
    private Long milestoneId;

    @Schema(description = "Нужно ли строго соблюдать количество пропущенных в следующий этап конкурсантов", example = "false")
    private Boolean strictPassMode;
}
