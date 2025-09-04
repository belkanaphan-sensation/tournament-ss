package org.bn.sensation.core.milestone.service.dto;

import org.bn.sensation.core.common.dto.BaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Правило вехи")
public class MilestoneRuleDto extends BaseDto {
}
