package org.bn.sensation.core.criteria.service.dto;

import java.math.BigDecimal;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.PartnerSide;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на создание назначения критерия оценки этапу")
public class CreateMilestoneCriteriaAssignmentRequest extends EmptyDto {

    @NotNull
    @Schema(description = "ID правила этапа", example = "1")
    private Long milestoneRuleId;

    @NotNull
    @Schema(description = "ID критерия оценки", example = "1")
    private Long criteriaId;

    @Schema(description = "Сторона в соревновании, к которой относится критерий в рамках этапа", example = "LEADER")
    private PartnerSide partnerSide;

    @Schema(description = "Вес критерия в рамках этапа", example = "1.0")
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Positive
    @Schema(description = "Максимальный балл шкалы для критерия в рамках этапа", example = "10")
    @NotNull
    private Integer scale;

}
