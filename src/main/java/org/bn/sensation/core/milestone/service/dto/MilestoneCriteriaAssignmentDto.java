package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.Gender;

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

    @Schema(description = "Пол, к которому относится критерий в рамках этапа", example = "MALE")
    private Gender gender;
}
