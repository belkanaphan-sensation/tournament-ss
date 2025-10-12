package org.bn.sensation.core.criteria.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneCriteriaAssignmentRepository extends BaseRepository<MilestoneCriteriaAssignmentEntity> {

    @EntityGraph(attributePaths = {"milestoneRule", "criteria"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca " +
            "WHERE mca.milestoneRule.milestone.id = :milestoneId " +
            "AND mca.criteria.id = :criteriaId")
    Optional<MilestoneCriteriaAssignmentEntity> findByMilestoneIdAndCriteriaId(Long milestoneId, Long criteriaId);

    @EntityGraph(attributePaths = {"milestoneRule", "criteria"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca " +
            "WHERE mca.milestoneRule.id = :milestoneRuleId " +
            "AND mca.criteria.id = :criteriaId")
    Optional<MilestoneCriteriaAssignmentEntity> findByMilestoneRuleIdAndCriteriaId(Long milestoneRuleId, Long criteriaId);

    @EntityGraph(attributePaths = {"milestoneRule", "criteria"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca WHERE mca.milestoneRule.milestone.id = :milestoneId")
    List<MilestoneCriteriaAssignmentEntity> findByMilestoneId(Long milestoneId);

    @EntityGraph(attributePaths = {"milestoneRule", "criteria"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca WHERE mca.milestoneRule.id = :milestoneRuleId")
    List<MilestoneCriteriaAssignmentEntity> findByMilestoneRuleId(Long milestoneRuleId);

    @EntityGraph(attributePaths = {"milestoneRule", "criteria"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca WHERE mca.criteria.id = :criteriaId")
    List<MilestoneCriteriaAssignmentEntity> findByCriteriaId(Long criteriaId);

    @EntityGraph(attributePaths = {"milestoneRule"})
    @Query("SELECT mca FROM MilestoneCriteriaAssignmentEntity mca WHERE mca.id = :id")
    Optional<MilestoneCriteriaAssignmentEntity> findByIdWithRule(Long id);

    boolean existsByMilestoneRuleIdAndCriteriaId(Long milestoneRuleId, Long criteriaId);

    boolean existsByCriteriaId(Long criteriaId);

}
