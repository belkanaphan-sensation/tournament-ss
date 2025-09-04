package org.bn.sensation.core.role.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Роль пользователя в системе")
public class RoleDto extends BaseDto {

    @Schema(description = "Имя роли", example = "ADMIN")
    private String role;
}
