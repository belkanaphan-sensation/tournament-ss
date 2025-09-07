package org.bn.sensation.core.round.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;

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

    @Schema(description = "Описание раунда", example = "Матчи по круговой системе")
    private String description;

    @Schema(description = "Этап, к которому принадлежит раунд")
    private EntityLinkDto milestone;

    @Schema(description = "Список участников раунда")
    private Set<EntityLinkDto> participants;
}
