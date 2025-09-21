package org.bn.sensation.core.occasion.service.dto;

import java.time.LocalDate;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.Status;

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
@Schema(description = "Мероприятие (высокоуровневое событие, содержащее активности)")
public class OccasionDto extends BaseDto {

    @Schema(description = "Название мероприятия", example = "SBF")
    private String name;

    @Schema(description = "Описание мероприятия", example = "Весенний фестиваль боевых искусств")
    private String description;

    @Schema(description = "Дата начала", example = "2025-04-13")
    private LocalDate startDate;

    @Schema(description = "Дата окончания", example = "2025-04-15")
    private LocalDate endDate;

    @Schema(description = "Организация, проводящая мероприятие")
    private EntityLinkDto organization;

    @Schema(description = "Статус мероприятия", example = "DRAFT")
    private Status status;
}
