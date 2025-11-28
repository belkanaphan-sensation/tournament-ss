package org.bn.sensation.core.milestoneresult.service.dto;

import java.util.List;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
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
@Schema(description = "Результат этапа для конкурсанта")
public class MilestoneResultDto extends BaseDto {

    @Schema(description = "Этап")
    private EntityLinkDto milestone;

    @Schema(description = "Конкурсант")
    private ContestantDto contestant;

    @Schema(description = "Прошел конкурсант в следующий этап или нет по решению организатора")
    private Boolean finallyApproved;

    @Schema(description = "Прошел участник в следующий этап или нет по результатам оценки последнего раунда для данного конкурсанта")
    private PassStatus judgePassed;

    @Schema(description = "Результаты раундов для этапа")
    private List<MilestoneRoundResultDto> milestoneRoundResults;
}
