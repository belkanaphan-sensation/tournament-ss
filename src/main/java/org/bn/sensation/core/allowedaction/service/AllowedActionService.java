package org.bn.sensation.core.allowedaction.service;

import org.bn.sensation.core.allowedaction.service.dto.AllowedActionDto;

import jakarta.validation.constraints.NotNull;

public interface AllowedActionService {

    AllowedActionDto getForActivity(@NotNull Long activityId);

    AllowedActionDto getForOccasion(@NotNull Long occasionId);

    AllowedActionDto getForMilestone(@NotNull Long milestoneId);
}
