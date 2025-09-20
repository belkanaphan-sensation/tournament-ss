package org.bn.sensation.core.occasion.service.dto;

import java.time.ZonedDateTime;

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

    @Schema(description = "Дата начала", example = "2025-09-18T16:56:47+04:00[Europe/Samara]")
    private ZonedDateTime startDate;

    @Schema(description = "Дата окончания", example = "2025-09-18T16:56:47+04:00[Europe/Samara]")
    private ZonedDateTime endDate;

    @Schema(description = "Организация, проводящая мероприятие")
    private EntityLinkDto organization;

    @Schema(description = "Статус мероприятия", example = "DRAFT")
    private Status status;
}
