package org.bn.sensation.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;

@NoArgsConstructor
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseDto {

    @Schema(description = "Id в БД", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    @Min(value = 0, message = "ID должен быть больше или равно 0")
    private Long id;
}
