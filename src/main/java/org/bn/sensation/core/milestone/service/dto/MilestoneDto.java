package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.Status;

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
@Schema(description = "Веха (логический этап в рамках активности)")
public class MilestoneDto extends BaseDto {

    @Schema(description = "Название этапа'", example = "Квалификация")
    private String name;

    @Schema(description = "Описание этапа", example = "Квалификационный этап соревнования")
    private String description;

    @Schema(description = "Активность, частью которой является этап")
    private EntityLinkDto activity;

    @Schema(description = "Статус этапа", example = "DRAFT")
    private Status status;

    @Schema(description = "Количество завершенных раундов", example = "3")
    private Long completedRoundsCount;

    @Schema(description = "Общее количество раундов в этапе", example = "5")
    private Long totalRoundsCount;

    @Schema(description = "Порядок этапа в рамках активности", example = "1")
    private Integer milestoneOrder;
}
