package org.bn.sensation.core.contestant.service.dto;

import java.util.List;
import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.contestant.entity.ContestantType;

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
@Schema(description = "Конкурсант")
public class ContestantDto extends BaseDto {

    @Schema(description = "Стартовый номер конкурсанта", example = "A-102")
    private String number;

    @Schema(description = "Тип конкурсанта", example = "COUPLE")
    private ContestantType contestantType;

    @Schema(description = "Список участников ассоциированных с конкурсантом")
    private Set<ContestParticipantDto> participants;

    @Schema(description = "Список этапов конкурсанта")
    private List<EntityLinkDto> milestones;

    @Schema(description = "Список раундов конкурсанта")
    private Set<EntityLinkDto> rounds;
}
