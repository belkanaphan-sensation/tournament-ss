package org.bn.sensation.core.user.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.PersonDto;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.role.service.dto.RoleDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Пользователь системы")
public class UserDto extends BaseDto {

    @Schema(description = "Имя пользователя (уникальное)", example = "john_doe")
    private String username;

    @Schema(description = "Статус пользователя", example = "ACTIVE")
    private String status;

    @Schema(description = "Личная информация")
    private PersonDto person;

    @Schema(description = "Организация пользователя")
    private OrganizationDto organization;

    @Schema(description = "Список ролей пользователя")
    private Set<RoleDto> roles;
}
