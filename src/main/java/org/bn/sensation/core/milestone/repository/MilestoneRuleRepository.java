package org.bn.sensation.core.milestone.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface MilestoneRuleRepository extends BaseRepository <MilestoneRuleEntity>{

    @EntityGraph(attributePaths = {"milestone"})
    @Query("SELECT mr FROM MilestoneRuleEntity mr WHERE mr.id = :id")
    Optional<MilestoneRuleEntity> findByIdWithMilestone(Long id);

    @EntityGraph(attributePaths = {"milestone"})
    @Query("SELECT mr FROM MilestoneRuleEntity mr WHERE mr.milestone.id = :milestoneId")
    Optional<MilestoneRuleEntity> findByMilestoneId(Long milestoneId);
}
