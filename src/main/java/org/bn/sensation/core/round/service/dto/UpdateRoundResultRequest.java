package org.bn.sensation.core.round.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateRoundResultRequest extends EmptyDto {

    @PositiveOrZero
    @NotNull
    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;
}
