package org.bn.sensation.core.useractivity.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.entity.UserActivityPosition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface UserActivityAssignmentRepository extends BaseRepository<UserActivityAssignmentEntity> {

    Optional<UserActivityAssignmentEntity> findByUserIdAndActivityId(Long userId, Long activityId);

    List<UserActivityAssignmentEntity> findByUserId(Long userId);

    List<UserActivityAssignmentEntity> findByActivityId(Long activityId);

    List<UserActivityAssignmentEntity> findByPosition(UserActivityPosition position);

    List<UserActivityAssignmentEntity> findByActivityIdAndPosition(Long activityId, UserActivityPosition position);

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    long countByActivityIdAndPosition(Long activityId, UserActivityPosition position);

    void deleteByActivityId(Long activityId);

    @EntityGraph(attributePaths = {"user", "activity"})
    @Query("""
            SELECT DISTINCT uaa
            FROM UserActivityAssignmentEntity uaa
            JOIN uaa.activity a
            WHERE uaa.user.id = :userId
              AND a.occasion.id = :occasionId
            """)
    List<UserActivityAssignmentEntity> findByUserIdAndOccasionId(Long userId, Long occasionId);
}
