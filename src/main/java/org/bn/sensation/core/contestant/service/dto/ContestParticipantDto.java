package org.bn.sensation.core.contestant.service.dto;

import org.bn.sensation.core.assistant.service.dto.AssistantDto;
import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.PartnerSide;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Участник внутри конкурсанта")
public class ContestParticipantDto extends EmptyDto {

    @Schema(description = "ID участника (внутри конкурсанта)")
    private Long participantId;

    @Schema(description = "Стартовый номер участника")
    private String number;

    @Schema(description = "Сторона участника")
    private PartnerSide partnerSide;

    @Schema(description = "Имя")
    private String name;

    @Schema(description = "Фамилия")
    private String surname;

    @Schema(description = "Ассистент")
    private AssistantDto assistant;
}
