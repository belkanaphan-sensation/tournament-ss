package org.bn.sensation.core.user.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.user.entity.UserActivityPosition;

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
@Schema(description = "Запрос на обновление назначения пользователя на активность")
public class UpdateUserActivityAssignmentRequest extends EmptyDto {

    @Schema(description = "Позиция пользователя в активности", example = "JUDGE_CHIEF")
    private UserActivityPosition position;

    @Schema(description = "Кого будет оценивать судья", example = "LEADER")
    private PartnerSide partnerSide;
}
