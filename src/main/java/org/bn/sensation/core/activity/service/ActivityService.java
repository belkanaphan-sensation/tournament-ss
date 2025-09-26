package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.service.BaseCrudService;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public interface ActivityService extends BaseCrudService<
        ActivityEntity,
        ActivityDto,
        CreateActivityRequest,
        UpdateActivityRequest> {

    List<ActivityDto> findByOccasionId(@NotNull Long id);

    List<ActivityDto> findByOccasionIdInLifeStates(@NotNull Long id);
}
