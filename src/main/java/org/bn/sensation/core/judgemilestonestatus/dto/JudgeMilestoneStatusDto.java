package org.bn.sensation.core.judgemilestonestatus.dto;

import java.time.LocalDateTime;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.judgemilestonestatus.model.JudgeMilestoneStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Статус этапа по судьям")
public class JudgeMilestoneStatusDto {

    @Schema(description = "Судья. Юзер привязанный к активности")
    private EntityLinkDto judge;

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Статус этапа, проставленный судьей", example = "READY")
    private JudgeMilestoneStatus status;

    @Schema(description = "Время проверки")
    private LocalDateTime calculatedAt;

}
