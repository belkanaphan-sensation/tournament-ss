package org.bn.sensation.core.user.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
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
@Schema(description = "Назначение пользователя на активность с ролью")
public class UserActivityAssignmentDto extends BaseDto {

    @Schema(description = "Пользователь")
    private EntityLinkDto user;

    @Schema(description = "Активность")
    private EntityLinkDto activity;

    @Schema(description = "Должность пользователя в активности", example = "JUDGE_CHIEF")
    private UserActivityPosition position;

    @Schema(description = "Сторона, которую оценивает или к которой принадлежит юзер в соревновании", example = "LEADER")
    private PartnerSide partnerSide;
}
