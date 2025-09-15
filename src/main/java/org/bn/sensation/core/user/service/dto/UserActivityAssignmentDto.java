package org.bn.sensation.core.user.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.user.entity.UserActivityRole;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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

    @Schema(description = "Роль пользователя в активности", example = "JUDGE_CHIEF")
    private UserActivityRole role;

    @Schema(description = "Дата назначения")
    private LocalDateTime assignedAt;

    @Schema(description = "Имя пользователя")
    private String userName;

    @Schema(description = "Фамилия пользователя")
    private String userSurname;

    @Schema(description = "Отчество пользователя")
    private String userSecondName;
}
