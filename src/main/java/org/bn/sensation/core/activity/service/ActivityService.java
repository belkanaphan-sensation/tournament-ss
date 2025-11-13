package org.bn.sensation.core.activity.service;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.dto.CreateActivityResultRequest;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ActivityService extends BaseCrudService<
        ActivityEntity,
        ActivityDto,
        CreateActivityRequest,
        UpdateActivityRequest>{

    List<ActivityDto> findByOccasionId(@NotNull Long id);

    List<EntityLinkDto> findPlannedByOccasionId(@NotNull Long id);

    List<ActivityDto> findByOccasionIdInLifeStatesForCurrentUser(@NotNull Long id);

    void planActivity(Long id);

    void startActivity(Long id);

    List<ContestantDto> closeRegistrationToActivity(Long id);

    void completeActivity(Long id);

    List<ActivityResultDto> sumUpActivity(@NotNull Long id, @Valid List<CreateActivityResultRequest> request);
}
