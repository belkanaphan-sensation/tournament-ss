package org.bn.sensation.core.judgeroundstatus.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Статус раунда который проставляет судья")
public class JudgeRoundStatusDto extends BaseDto {

    @Schema(description = "Судья. Юзер привязанный к активности (UserActivityAssignmentEntity)")
    private EntityLinkDto judge;

    @Schema(description = "Раунд")
    private EntityLinkDto round;

    @Schema(description = "Статус раунда, проставленный судьей", example = "ACCEPTED")
    private JudgeRoundStatus status;
}
