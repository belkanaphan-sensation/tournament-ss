package org.bn.sensation.core.round.service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на формирование раундов с участниками")
public class GenerateRoundsRequest {

    @NotNull
    @Schema(description = "ID этапа в который должны быть добавлены участники")
    private Long milestoneId;

    @Schema(description = "Список ID участников. Если он присутствует, " +
            "то участники добавляются из данного списка, если нет, то все участники, " +
            "зарегистрированные в активности, которой принадлежит этап")
    private List<Long> participantIds;

    @Schema(description = "Признак, что для данного этапа нужно переформировать раунды" +
            " (удалить старые и создать новые с новыми данными)")
    private Boolean reGenerate;
}
