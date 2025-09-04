package org.bn.sensation.core.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDto {

    @Schema(description = "Имя", example = "Иван")
    private String name;

    @Schema(description = "Фамилия", example = "Иванов")
    private String surname;

    @Schema(description = "Отчество", example = "Иванович")
    private String secondName;

    @Schema(description = "Электронная почта", example = "ivan.ivanov@example.com")
    private String email;

    @Schema(description = "Номер телефона", example = "+7 777 123-45-67")
    private String phoneNumber;
}
