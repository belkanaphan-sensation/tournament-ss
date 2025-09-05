package org.bn.sensation.core.role.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на создание роли")
public class CreateRoleRequest {

    @NotBlank
    @Pattern(regexp = "^(SUPERADMIN|ADMIN|OCCASION_ADMIN|USER|READER)$",
             message = "Role must be one of: SUPERADMIN, ADMIN, OCCASION_ADMIN, USER, READER")
    @Schema(description = "Название роли", example = "ADMIN",
            allowableValues = {"SUPERADMIN", "ADMIN", "OCCASION_ADMIN", "USER", "READER"})
    private String role;
}
