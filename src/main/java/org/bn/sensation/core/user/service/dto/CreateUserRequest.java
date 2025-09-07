package org.bn.sensation.core.user.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Запрос на создание пользователя")
public class CreateUserRequest extends EmptyDto {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Имя пользователя (уникальное)", example = "john_doe")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Пароль", example = "password123")
    private String password;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Имя", example = "Иван")
    private String name;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Фамилия", example = "Иванов")
    private String surname;

    @Size(max = 100)
    @Schema(description = "Отчество", example = "Иванович")
    private String secondName;

    @Email
    @Size(max = 255)
    @Schema(description = "Email", example = "ivan@example.com")
    private String email;

    @Size(max = 20)
    @Schema(description = "Номер телефона", example = "+7 777 123 45 67")
    private String phoneNumber;

    @NotNull
    @Schema(description = "Статус пользователя", example = "ACTIVE")
    private String status;

    @Schema(description = "ID организации")
    private Long organizationId;

    @Schema(description = "Список ID ролей")
    private Set<Long> roleIds;
}
