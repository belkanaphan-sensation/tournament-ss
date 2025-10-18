package org.bn.sensation.core.round.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.statemachine.state.RoundState;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Раунд в рамках активности/соревнования")
public class RoundDto extends BaseDto {

    @Schema(description = "Название раунда", example = "Групповой этап")
    private String name;

    @Schema(description = "Aктивность, к которому принадлежит этап")
    private EntityLinkDto activity;

    @Schema(description = "Этап, к которому принадлежит раунд")
    private EntityLinkDto milestone;

    @Schema(description = "Список участников раунда")
    private Set<EntityLinkDto> participants;

    @Schema(description = "Статус раунда", example = "DRAFT")
    private RoundState state;

    @Schema(description = "Дополнительный раунд", example = "true")
    private Boolean isExtraRound;

    @Schema(description = "Порядок раунда в рамках этапа. Прямая последовательность. 0 - первый раунд", example = "1")
    private Integer roundOrder;
}
