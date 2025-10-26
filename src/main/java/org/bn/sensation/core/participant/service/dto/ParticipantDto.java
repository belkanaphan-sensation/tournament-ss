package org.bn.sensation.core.participant.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.dto.PersonDto;
import org.bn.sensation.core.common.entity.PartnerSide;

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
@Schema(description = "Участник активности")
public class ParticipantDto extends BaseDto {

    @Schema(description = "Личные данные участника")
    private PersonDto person;

    @Schema(description = "Стартовый номер участника", example = "A-102")
    private String number;

    @Schema(description = "Сторона участника в соревновании", example = "LEADER")
    private PartnerSide partnerSide;

    @Schema(description = "Пометка о том что участник прошел регистрацию")
    private Boolean isRegistered;

    @Schema(description = "Активность участника")
    private EntityLinkDto activity;

    @Schema(description = "Список этапов")
    private Set<EntityLinkDto> milestones;

    @Schema(description = "Список раундов участника")
    private Set<EntityLinkDto> rounds;
}
