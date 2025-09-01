package org.bn.sensation.core.round.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Раунд в рамках активности/соревнования")
public class RoundDto extends BaseDto {

    @Schema(description = "Название раунда", example = "Групповой этап")
    private String name;

    @Schema(description = "Описание раунда", example = "Матчи по круговой системе")
    private String description;
}
