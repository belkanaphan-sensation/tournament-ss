package org.bn.sensation.core.activity.service.dto;

import java.time.LocalDateTime;

import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.activity.statemachine.ActivityState;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Активность (событие в рамках мероприятия)")
public class ActivityDto extends BaseDto {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Название активности", example = "Открытие турнира")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Описание активности", example = "Церемония открытия, приветственное слово организаторов")
    private String description;

    @Schema(description = "Дата и время начала", example = "2025-09-15T10:00:00")
    private LocalDateTime startDateTime;

    @Future(message = "Время окончания должно быть в будущем")
    @Schema(description = "Дата и время окончания", example = "2025-09-15T12:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Адрес проведения")
    private AddressDto address;

    @Schema(description = "Мероприятие, частью которого является активность")
    private EntityLinkDto occasion;

    @Schema(description = "Статус активности", example = "DRAFT")
    private ActivityState state;

    @Schema(description = "Количество завершенных этапов", example = "3")
    private Integer completedMilestonesCount;

    @Schema(description = "Общее количество этапов в активности", example = "5")
    private Integer totalMilestonesCount;
}
