package org.bn.sensation.core.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDto {

    @Schema(description = "Страна", example = "Казахстан")
    private String country;

    @Schema(description = "Город", example = "Алматы")
    private String city;

    @Schema(description = "Улица", example = "Абая")
    private String streetName;

    @Schema(description = "Номер дома", example = "10А")
    private String streetNumber;

    @Schema(description = "Комментарий к адресу", example = "Вход со двора")
    private String comment;
}
