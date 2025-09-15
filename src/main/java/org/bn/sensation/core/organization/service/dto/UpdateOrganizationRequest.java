package org.bn.sensation.core.organization.service.dto;

import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Запрос на обновление организации")
public class UpdateOrganizationRequest extends EmptyDto {

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
