package org.bn.sensation.core.organization.service.dto;

import org.bn.sensation.core.common.dto.AddressDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на создание организации")
public class CreateOrganizationRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название организации", example = "Sense")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание организации", example = "Организация, проводящая турниры и события")
    private String description;

    @Size(max = 20)
    @Schema(description = "Контактный номер телефона", example = "+7 777 123 45 67")
    private String phoneNumber;

    @Email
    @Size(max = 255)
    @Schema(description = "Электронная почта", example = "info@sense.kz")
    private String email;

    @Schema(description = "Адрес организации")
    private AddressDto address;
}
