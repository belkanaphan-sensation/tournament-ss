package org.bn.sensation.core.judge.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;

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
@Schema(description = "Статус этапа по судьям")
public class JudgeMilestoneStatusDto extends BaseDto {

    @Schema(description = "Судья. Юзер привязанный к активности (UserActivityAssignmentEntity)")
    private EntityLinkDto judge;

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Статус этапа, проставленный судьей", example = "ACCEPTED")
    private JudgeMilestoneStatus status;
}
