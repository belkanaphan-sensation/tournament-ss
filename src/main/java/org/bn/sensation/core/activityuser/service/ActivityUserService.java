package org.bn.sensation.core.activityuser.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.activityuser.service.dto.CreateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.UpdateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.ActivityUserDto;

import jakarta.validation.constraints.NotNull;

public interface ActivityUserService extends BaseCrudService<
        ActivityUserEntity,
        ActivityUserDto,
        CreateActivityUserRequest,
        UpdateActivityUserRequest> {

    // Custom operations
    ActivityUserDto findByUserIdAndActivityId(@NotNull Long userId, @NotNull Long activityId);

    List<ActivityUserDto> findByUserId(@NotNull Long userId);

    List<ActivityUserDto> findByActivityId(@NotNull Long activityId);

    List<ActivityUserDto> findByPosition(@NotNull UserActivityPosition position);

    List<ActivityUserDto> findByActivityIdAndPosition(@NotNull Long activityId, @NotNull UserActivityPosition position);

    ActivityUserDto findByActivityIdForCurrentUser(@NotNull Long activityId);

    List<ActivityUserDto> findByOccasionIdForCurrentUser(@NotNull Long occasionId);
}
