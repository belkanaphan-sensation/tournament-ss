package org.bn.sensation.core.useractivity.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.entity.UserActivityPosition;
import org.bn.sensation.core.useractivity.service.dto.CreateUserActivityAssignmentRequest;
import org.bn.sensation.core.useractivity.service.dto.UpdateUserActivityAssignmentRequest;
import org.bn.sensation.core.useractivity.service.dto.UserActivityAssignmentDto;

import jakarta.validation.constraints.NotNull;

public interface UserActivityAssignmentService extends BaseCrudService<
        UserActivityAssignmentEntity,
        UserActivityAssignmentDto,
        CreateUserActivityAssignmentRequest,
        UpdateUserActivityAssignmentRequest> {

    // Custom operations
    UserActivityAssignmentDto findByUserIdAndActivityId(@NotNull Long userId, @NotNull Long activityId);

    List<UserActivityAssignmentDto> findByUserId(@NotNull Long userId);

    List<UserActivityAssignmentDto> findByActivityId(@NotNull Long activityId);

    List<UserActivityAssignmentDto> findByPosition(@NotNull UserActivityPosition position);

    List<UserActivityAssignmentDto> findByActivityIdAndPosition(@NotNull Long activityId, @NotNull UserActivityPosition position);

    UserActivityAssignmentDto findByActivityIdForCurrentUser(@NotNull Long activityId);

    List<UserActivityAssignmentDto> findByOccasionIdForCurrentUser(@NotNull Long occasionId);
}
