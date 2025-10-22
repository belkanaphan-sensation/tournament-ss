package org.bn.sensation.core.milestonecriterion.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneCriterionRepository extends BaseRepository<MilestoneCriterionEntity> {

    @EntityGraph(attributePaths = {"milestoneRule", "criterion"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc " +
            "WHERE mc.milestoneRule.milestone.id = :milestoneId " +
            "AND mc.criterion.id = :criterionId")
    Optional<MilestoneCriterionEntity> findByMilestoneIdAndCriterionId(Long milestoneId, Long criterionId);

    @EntityGraph(attributePaths = {"milestoneRule", "criterion"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc " +
            "WHERE mc.milestoneRule.id = :milestoneRuleId " +
            "AND mc.criterion.id = :criterionId")
    Optional<MilestoneCriterionEntity> findByMilestoneRuleIdAndCriterionId(Long milestoneRuleId, Long criterionId);

    @EntityGraph(attributePaths = {"milestoneRule", "criterion"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc WHERE mc.milestoneRule.milestone.id = :milestoneId")
    List<MilestoneCriterionEntity> findByMilestoneId(Long milestoneId);

    @EntityGraph(attributePaths = {"milestoneRule", "criterion"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc WHERE mc.milestoneRule.id = :milestoneRuleId")
    List<MilestoneCriterionEntity> findByMilestoneRuleId(Long milestoneRuleId);

    @EntityGraph(attributePaths = {"milestoneRule", "criterion"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc WHERE mc.criterion.id = :criterionId")
    List<MilestoneCriterionEntity> findByCriterionId(Long criterionId);

    @EntityGraph(attributePaths = {"milestoneRule"})
    @Query("SELECT mc FROM MilestoneCriterionEntity mc WHERE mc.id = :id")
    Optional<MilestoneCriterionEntity> findByIdWithRule(Long id);

    boolean existsByMilestoneRuleIdAndCriterionId(Long milestoneRuleId, Long criterionId);

    boolean existsByCriterionId(Long criterionId);

}
