package org.bn.sensation.core.activityuser.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Запрос на создание назначения пользователя на активность")
public class CreateActivityUserRequest extends EmptyDto {

    @NotNull
    @Schema(description = "ID пользователя", example = "1")
    private Long userId;

    @NotNull
    @Schema(description = "ID активности", example = "1")
    private Long activityId;

    @NotNull
    @Schema(description = "Должность пользователя в активности", example = "JUDGE_CHIEF")
    private UserActivityPosition position;

    @Schema(description = "Кого будет оценивать судья", example = "LEADER")
    private PartnerSide partnerSide;
}
