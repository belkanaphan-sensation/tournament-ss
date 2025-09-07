package org.bn.sensation.core.role.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Запрос на обновление роли")
public class UpdateRoleRequest extends EmptyDto {

    @NotBlank
//    @Pattern(regexp = "^(SUPERADMIN|ADMIN|OCCASION_ADMIN|USER|READER)$",
//             message = "Role must be one of: SUPERADMIN, ADMIN, OCCASION_ADMIN, USER, READER")
//    @Schema(description = "Название роли", example = "ADMIN",
//            allowableValues = {"SUPERADMIN", "ADMIN", "OCCASION_ADMIN", "USER", "READER"})
    @Schema(description = "Название роли", example = "ADMIN")
    private String role;
}
