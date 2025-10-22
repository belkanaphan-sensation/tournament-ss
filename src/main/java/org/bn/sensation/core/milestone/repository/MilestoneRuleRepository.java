package org.bn.sensation.core.milestone.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.EntityNotFoundException;

public interface MilestoneRuleRepository extends BaseRepository<MilestoneRuleEntity> {

    @EntityGraph(attributePaths = {"milestone"})
    @Query("SELECT mr FROM MilestoneRuleEntity mr WHERE mr.id = :id")
    Optional<MilestoneRuleEntity> findByIdWithMilestone(Long id);

    default MilestoneRuleEntity getByIdWithMilestoneOrThrow(Long id) {
        return findByIdWithMilestone(id).orElseThrow(() -> new EntityNotFoundException("Правило этапа не найдено: " + id));
    }

    @EntityGraph(attributePaths = {"milestone"})
    @Query("SELECT mr FROM MilestoneRuleEntity mr WHERE mr.milestone.id = :milestoneId")
    Optional<MilestoneRuleEntity> findByMilestoneId(Long milestoneId);

    default MilestoneRuleEntity getByMilestoneIdOrThrow(Long milestoneId) {
        return findByMilestoneId(milestoneId).orElseThrow(() -> new EntityNotFoundException("Правило не найдено для этапа: " + milestoneId));
    }

    @EntityGraph(attributePaths = {"milestoneCriteria"})
    @Query("SELECT mr FROM MilestoneRuleEntity mr WHERE mr.id = :id")
    Optional<MilestoneRuleEntity> findByIdWithCriteria(Long id);

    default MilestoneRuleEntity getByIdWithCriteriaOrThrow(Long id) {
        return findByIdWithCriteria(id).orElseThrow(() -> new EntityNotFoundException("Правило этапа не найдено: " + id));
    }

    default MilestoneRuleEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Правило этапа не найдено: " + id));
    }
}
