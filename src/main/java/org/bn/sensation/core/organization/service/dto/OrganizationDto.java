package org.bn.sensation.core.organization.service.dto;

import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.dto.BaseDto;

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
@Schema(description = "Организация (владелец/проводящая сторона мероприятия)")
public class OrganizationDto extends BaseDto {

    @Schema(description = "Название организации", example = "Sense")
    private String name;

    @Schema(description = "Описание организации", example = "Организация, проводящая турниры и события")
    private String description;

    @Schema(description = "Контактный номер телефона", example = "+7 777 123 45 67")
    private String phoneNumber;

    @Schema(description = "Электронная почта", example = "info@sense.kz")
    private String email;

    @Schema(description = "Адрес организации")
    private AddressDto address;
}
