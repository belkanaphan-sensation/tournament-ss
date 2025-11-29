package org.bn.sensation.core.user.service.dto;

import org.bn.sensation.core.user.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegistrationRequest(
        @Schema(description = "Логин", example = "john_doe")
        @NotBlank String username,

        @Schema(description = "Пароль", example = "P@ssw0rd!")
        @NotBlank String password,

        @Schema(description = "Имя", example = "John")
        @NotBlank String name,

        @Schema(description = "Фамилия", example = "Иванов")
        String surname,

        @Schema(description = "Отчество", example = "Иванович")
        String secondName,

        @Schema(description = "Имейл", example = "john@example.com")
        @Email String email,

        @Schema(description = "Номер телефона", example = "+7 777 123-45-67")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
        String phoneNumber,

        @NotNull
        @Schema(description = "Роль пользователя", example = "USER")
        Role role
) {
}
