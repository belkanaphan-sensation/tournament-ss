package org.bn.sensation.core.round.service.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на обновление раунда")
public class UpdateRoundRequest {

    @Size(max = 255)
    @Schema(description = "Название раунда", example = "Первый раунд")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание раунда", example = "Квалификационный раунд")
    private String description;

    @Schema(description = "ID вехи, частью которой является раунд")
    private Long milestoneId;

    @Schema(description = "Список ID участников")
    private Set<Long> participantIds;
}
