package org.bn.sensation.core.activityuser.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.EntityNotFoundException;

public interface ActivityUserRepository extends BaseRepository<ActivityUserEntity> {

    Optional<ActivityUserEntity> findByUserIdAndActivityId(Long userId, Long activityId);

    default ActivityUserEntity getByUserIdAndActivityIdOrThrow(Long userId, Long activityId) {
        return findByUserIdAndActivityId(userId, activityId).orElseThrow(() -> new EntityNotFoundException("Назначение пользователя не найдено для userId %s activityId %s".formatted(userId, activityId)));
    }

    List<ActivityUserEntity> findByUserId(Long userId);

    List<ActivityUserEntity> findByActivityId(Long activityId);

    List<ActivityUserEntity> findByPosition(UserActivityPosition position);

    List<ActivityUserEntity> findByActivityIdAndPosition(Long activityId, UserActivityPosition position);

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    long countByActivityIdAndPosition(Long activityId, UserActivityPosition position);

    void deleteByActivityId(Long activityId);

    @EntityGraph(attributePaths = {"user", "activity"})
    @Query("""
            SELECT DISTINCT au
            FROM ActivityUserEntity au
            JOIN au.activity a
            WHERE au.user.id = :userId
              AND a.occasion.id = :occasionId
            """)
    List<ActivityUserEntity> findByUserIdAndOccasionId(Long userId, Long occasionId);

    default ActivityUserEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Назначение пользователя не найдено: " + id));
    }
}
