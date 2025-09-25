package org.bn.sensation.core.user.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface UserActivityAssignmentRepository extends BaseRepository<UserActivityAssignmentEntity> {

    Optional<UserActivityAssignmentEntity> findByUserIdAndActivityId(Long userId, Long activityId);

    Page<UserActivityAssignmentEntity> findByUserId(Long userId, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByActivityId(Long activityId, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByPosition(UserActivityPosition position, Pageable pageable);

    Page<UserActivityAssignmentEntity> findByActivityIdAndPosition(Long activityId, UserActivityPosition position, Pageable pageable);

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    long countByActivityIdAndPosition(Long activityId, UserActivityPosition position);

    @Query("SELECT uaa FROM UserActivityAssignmentEntity uaa " +
           "JOIN uaa.activity a " +
           "WHERE uaa.user.id = :userId AND a.occasion.id = :occasionId")
    Optional<UserActivityAssignmentEntity> findByUserIdAndOccasionId(Long userId, Long occasionId);
}
