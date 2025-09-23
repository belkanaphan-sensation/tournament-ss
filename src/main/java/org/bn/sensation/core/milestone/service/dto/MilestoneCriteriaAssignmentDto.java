package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.PartnerSide;

import java.math.BigDecimal;

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
@Schema(description = "Назначение критерия оценки этапу")
public class MilestoneCriteriaAssignmentDto extends BaseDto {

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Критерий оценки")
    private EntityLinkDto criteria;

    @Schema(description = "Сторона в соревновании, к которой относится критерий в рамках этапа", example = "LEADER")
    private PartnerSide partnerSide;

    @Schema(description = "Вес критерия в рамках этапа", example = "1.0")
    private BigDecimal weight;

    @Schema(description = "Максимальный балл шкалы для критерия в рамках этапа", example = "10")
    private Integer scale;
}
