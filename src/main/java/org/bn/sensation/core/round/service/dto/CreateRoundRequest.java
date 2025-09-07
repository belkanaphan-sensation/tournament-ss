package org.bn.sensation.core.round.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на создание раунда")
public class CreateRoundRequest extends EmptyDto {

    @Size(max = 255)
    @Schema(description = "Название раунда", example = "Первый раунд")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание раунда", example = "Квалификационный раунд")
    private String description;

    @Schema(description = "ID вехи, частью которой является раунд")
    private Long milestoneId;
}
