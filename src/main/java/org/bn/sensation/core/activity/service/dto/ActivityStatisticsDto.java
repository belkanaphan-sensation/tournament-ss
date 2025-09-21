package org.bn.sensation.core.activity.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Статистика этапов активности")
public class ActivityStatisticsDto {

    @Schema(description = "ID активности", example = "1")
    private Long activityId;

    @Schema(description = "Название активности", example = "Открытие турнира")
    private String activityName;

    @Schema(description = "Количество завершенных этапов", example = "3")
    private Long completedMilestonesCount;

    @Schema(description = "Общее количество этапов в активности", example = "5")
    private Long totalMilestonesCount;
}
