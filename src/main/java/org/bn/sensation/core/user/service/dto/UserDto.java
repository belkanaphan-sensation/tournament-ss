package org.bn.sensation.core.user.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.common.dto.PersonDto;

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
@Schema(description = "Пользователь системы")
public class UserDto extends BaseDto {

    @Schema(description = "Имя пользователя (уникальное)", example = "john_doe")
    private String username;

    //todo: временно показываем. На проде удалить
    @Schema(description = "Пароль", example = "password")
    private String password;

    @Schema(description = "Статус пользователя", example = "ACTIVE")
    private String status;

    @Schema(description = "Личная информация")
    private PersonDto person;

    @Schema(description = "Список организаций пользователя")
    private Set<EntityLinkDto> organizations;

    @Schema(description = "Список ролей пользователя")
    private Set<Role> roles;
}
