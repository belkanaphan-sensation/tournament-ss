package org.bn.sensation.core.milestoneresult.service.dto;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.milestoneresult.entity.PassStatus;

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
@Schema(description = "Запрос на обновление результата этапа")
public class UpdateMilestoneResultRequest extends EmptyDto {

    @Schema(description = "Прошел участник в следующий этап или нет по результатам оценок")
    private PassStatus judgePassed;

    @Schema(description = "Прошел участник в следующий этап или нет по решению организатора")
    private Boolean finallyApproved;

    @Schema(description = "Суммарный балл этапа для участника", example = "5")
    private Integer totalScore;
}
