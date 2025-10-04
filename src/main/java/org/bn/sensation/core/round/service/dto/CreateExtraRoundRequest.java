package org.bn.sensation.core.round.service.dto;

import java.util.List;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Запрос на создание дополнительного раунда")
public class CreateExtraRoundRequest extends EmptyDto {

    @NotBlank
    @Schema(description = "Название дополнительного раунда", example = "Дополнительный раунд для лидеров", required = true)
    private String name;

    @NotNull
    @Schema(description = "ID этапа", example = "1", required = true)
    private Long milestoneId;

    @Schema(description = "Список ID участников для дополнительного раунда")
    private List<Long> participantIds;
}
