package org.bn.sensation.core.round.service.dto;

import org.bn.sensation.core.judge.entity.JudgeRoundStatus;

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
@Schema(description = "Раунд в рамках активности/соревнования")
public class RoundWithJRStatusDto extends RoundDto {

    @Schema(description = "Статус раунда для текущего пользователя", example = "true")
    private JudgeRoundStatus judgeRoundStatus;
}
