package org.bn.sensation.core.contestant.service.dto;

import java.util.List;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Запрос на создание конкурсанта")
public class CreateContestantRequest extends EmptyDto {

    @NotNull
    @Schema(description = "ID этапа", example = "1", required = true)
    private Long milestoneId;

    @Schema(description = "ID раунда этапа", example = "1", required = true)
    private Long roundId;

    @NotNull
    @Schema(description = "Список ID участников, ассоциированных с этим конкурсантом")
    private List<Long> participantIds;
}
