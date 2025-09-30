package org.bn.sensation.core.round.service.dto;

import java.util.List;

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
@Schema(description = "Дополнительный раунд")
public class ExtraRoundDto extends BaseDto {

    @Schema(description = "Название дополнительного раунда", example = "Дополнительный раунд для лидеров")
    private String name;

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Состояние раунда", example = "DRAFT")
    private RoundState state;

    @Schema(description = "Участники дополнительного раунда")
    private List<EntityLinkDto> participants;
}
