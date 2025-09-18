package org.bn.sensation.core.occasion.service.dto;

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
@Schema(description = "Статистика активностей мероприятия")
public class OccasionStatisticsDto {

    @Schema(description = "ID мероприятия", example = "1")
    private Long occasionId;

    @Schema(description = "Название мероприятия", example = "SBF")
    private String occasionName;

    @Schema(description = "Количество активностей в состоянии 'Completed'", example = "5")
    private Long completedActivitiesCount;

    @Schema(description = "Количество активностей в активном состоянии (не Cancelled, Completed, Draft)", example = "3")
    private Long activeActivitiesCount;

    @Schema(description = "Общее количество активностей в мероприятии", example = "8")
    private Long totalActivitiesCount;
}
