package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.CompetitionRole;

import java.math.BigDecimal;

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
@Schema(description = "Запрос на обновление назначения критерия оценки этапу")
public class UpdateMilestoneCriteriaAssignmentRequest extends EmptyDto {

    @Schema(description = "ID этапа", example = "1")
    private Long milestoneId;

    @Schema(description = "ID критерия оценки", example = "1")
    private Long criteriaId;

    @Schema(description = "Роль в соревновании, к которой относится критерий в рамках этапа", example = "LEADER")
    private CompetitionRole competitionRole;

    @Schema(description = "Вес критерия в рамках этапа", example = "1.0")
    private BigDecimal weight;

    @Positive
    @Schema(description = "Максимальный балл шкалы для критерия в рамках этапа", example = "10")
    private Integer scale;
}
