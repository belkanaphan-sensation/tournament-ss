package org.bn.sensation.core.activity.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.EntityNotFoundException;

public interface ActivityRepository extends BaseRepository<ActivityEntity> {

    List<ActivityEntity> findByOccasionId(Long occasionId);

    @EntityGraph(attributePaths = {"occasion", "milestones"})
    @Query("""
            SELECT DISTINCT a
            FROM ActivityEntity a
            JOIN ActivityUserEntity au ON au.activity.id = a.id
            WHERE a.occasion.id = :occasionId
              AND au.user.id = :userId
              AND a.state IN :states
            """)
    List<ActivityEntity> findByOccasionIdAndUserIdAndStateIn(Long occasionId, Long userId, List<ActivityState> states);

    @EntityGraph(attributePaths = {"activityUsers"})
    @Query("SELECT a FROM ActivityEntity a WHERE a.id = :id")
    Optional<ActivityEntity> findByIdWithActivityUser(Long id);

    default ActivityEntity getByIdWithActivityUserOrThrow(Long id) {
        return findByIdWithActivityUser(id).orElseThrow(() -> new EntityNotFoundException("Активность не найдена: " + id));
    }

    default ActivityEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Активность не найдена: " + id));
    }
}
