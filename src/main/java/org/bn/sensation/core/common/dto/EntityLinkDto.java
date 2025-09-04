package org.bn.sensation.core.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class EntityLinkDto {

    @Schema(description = "Id сущности БД", example = "3")
    @Min(value = 0, message = "ID должен быть больше или равно 0")
    private Long id;

    @Schema(description = "Отображаемое поле", example = "SBF")
    private String value;
}
