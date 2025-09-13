package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.Status;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Запрос на создание вехи")
public class CreateMilestoneRequest extends EmptyDto {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название вехи", example = "Квалификация")
    private String name;

    @Schema(description = "ID активности, частью которой является веха")
    private Long activityId;

    @Schema(description = "Статус вехи", example = "DRAFT")
    private Status status;
}
