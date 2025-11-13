package org.bn.sensation.core.round.service.dto;

import java.util.List;

import org.bn.sensation.core.common.dto.EmptyDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Запрос на создание раунда")
public class CreateRoundRequest extends EmptyDto {

    @Size(max = 255)
    @Schema(description = "Название раунда", example = "Первый раунд")
    private String name;

    @Schema(description = "ID этапа, частью которой является раунд")
    @NotNull
    private Long milestoneId;

    @NotNull
    @Schema(description = "Список ID конкурсантов для раунда")
    private List<Long> contestantIds;
}
