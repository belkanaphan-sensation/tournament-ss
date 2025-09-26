package org.bn.sensation.core.round.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@Schema(description = "Запрос на создание результатов раунда")
public class CreateRoundResultRequest extends EmptyDto {

    @NotNull
    @Schema(description = "Участник")
    private Long participantId;

    @NotNull
    @Schema(description = "Раунд")
    private Long roundId;

    @NotNull
    @Schema(description = "Критерий, сформированный для данного этапа")
    private Long milestoneCriteriaId;

    @PositiveOrZero
    @NotNull
    @Schema(description = "Значение оценки для данного участника данным судьей по данному критерию", example = "5")
    private Integer score;
}
