package org.bn.sensation.core.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder(toBuilder = true)
public abstract class BaseDto extends EmptyDto{

  @Schema(description = "Id в БД", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
  @Min(value = 0, message = "ID должен быть больше или равно 0")
  private Long id;
}
