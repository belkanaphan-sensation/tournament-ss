package org.bn.sensation.core.user.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserActivityAssignmentRepository extends BaseRepository<UserActivityAssignmentEntity> {

    Optional<UserActivityAssignmentEntity> findByUserIdAndActivityId(Long userId, Long activityId);

    Page<UserActivityAssignmentEntity> findByUserId(Long userId, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByActivityId(Long activityId, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByRole(UserActivityRole role, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByActivityIdAndRole(Long activityId, UserActivityRole role, Pageable pageable);

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    long countByActivityIdAndRole(Long activityId, UserActivityRole role);
}
