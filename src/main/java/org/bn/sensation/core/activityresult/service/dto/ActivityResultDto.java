package org.bn.sensation.core.activityresult.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;

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
@Schema(description = "Результат активности для участника")
public class ActivityResultDto extends BaseDto {

    @Schema(description = "Участник")
    private EntityLinkDto activity;

    @Schema(description = "Участник")
    private EntityLinkDto participant;

    @Schema(description = "Место, который занял участник в активности", example = "1")
    private Integer place;
}
