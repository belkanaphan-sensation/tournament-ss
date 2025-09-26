package org.bn.sensation.core.participant.service.dto;

import java.util.Set;

import org.bn.sensation.core.common.dto.EmptyDto;
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
@Schema(description = "Раунд со списком участников")
public class RoundParticipantsDto extends EmptyDto {

    @Schema(description = "Раунд")
    private EntityLinkDto round;

    @Schema(description = "Список участников раунда")
    private Set<EntityLinkDto> participants;

}
