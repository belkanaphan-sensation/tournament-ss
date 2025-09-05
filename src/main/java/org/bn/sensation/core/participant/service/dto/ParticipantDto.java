package org.bn.sensation.core.participant.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.PersonDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
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
}
