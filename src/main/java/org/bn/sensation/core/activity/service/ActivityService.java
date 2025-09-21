package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.ActivityStatisticsDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.service.BaseCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.NotNull;

public interface ActivityService extends BaseCrudService<
        ActivityEntity,
        ActivityDto,
        CreateActivityRequest,
        UpdateActivityRequest> {

    Page<ActivityDto> findByOccasionId(@NotNull Long id, Pageable pageable);

    /**
     * Получить статистику этапов для активности
     *
     * @param activityId ID активности
     * @return статистика этапов
     */
    ActivityStatisticsDto getMilestoneStatistics(Long activityId);
}
