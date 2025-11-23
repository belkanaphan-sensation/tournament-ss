package org.bn.sensation.core.round.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;

public interface RoundRepository extends BaseRepository<RoundEntity> {

    List<RoundEntity> findByMilestoneId(Long milestoneId);

    @EntityGraph(attributePaths = {"milestone.activity.activityUsers.user"})
    @Query("SELECT r FROM RoundEntity r WHERE r.id = :id")
    Optional<RoundEntity> findByIdWithUser(@Param("id") Long id);

    default RoundEntity getByIdWithUserOrThrow(Long id) {
        return findByIdWithUser(id).orElseThrow(() -> new EntityNotFoundException("Раунд не найден: " + id));
    }

    @EntityGraph(attributePaths = {"milestone.activity.activityUsers.user", "milestone.milestoneRule.milestoneCriteria", "contestants"})
    @Query("SELECT r FROM RoundEntity r WHERE r.id = :id")
    Optional<RoundEntity> findByIdFull(@Param("id") Long id);

    default RoundEntity getByIdFullOrThrow(Long id) {
        return findByIdFull(id).orElseThrow(() -> new EntityNotFoundException("Раунд не найден: " + id));
    }

    @Query("SELECT max(r.roundOrder) FROM RoundEntity r WHERE r.milestone.id = :milestoneId")
    Optional<Integer> getLastRoundOrder(@Param("milestoneId") Long milestoneId);

    @Query("SELECT r FROM RoundEntity r WHERE r.milestone.id = :milestoneId AND r.roundOrder > :roundOrder")
    List<RoundEntity> findByMilestoneIdAndGtRoundOrder(@Param("milestoneId") Long milestoneId, @Param("roundOrder") Integer roundOrder);

    default RoundEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Раунд не найден: " + id));
    }
}
