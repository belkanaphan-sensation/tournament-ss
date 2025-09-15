package org.bn.sensation.core.user.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityRole;
import org.bn.sensation.core.user.service.dto.CreateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.NotNull;

public interface UserActivityAssignmentService extends BaseCrudService<
        UserActivityAssignmentEntity,
        UserActivityAssignmentDto,
        CreateUserActivityAssignmentRequest,
        UpdateUserActivityAssignmentRequest> {

    // Custom operations
    UserActivityAssignmentDto findByUserIdAndActivityId(@NotNull Long userId, @NotNull Long activityId);

    Page<UserActivityAssignmentDto> findByUserId(@NotNull Long userId, Pageable pageable);

    Page<UserActivityAssignmentDto> findByActivityId(@NotNull Long activityId, Pageable pageable);

    Page<UserActivityAssignmentDto> findByRole(@NotNull UserActivityRole role, Pageable pageable);

    Page<UserActivityAssignmentDto> findByActivityIdAndRole(@NotNull Long activityId, @NotNull UserActivityRole role, Pageable pageable);
}
