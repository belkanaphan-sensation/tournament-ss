package org.bn.sensation.core.milestone.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneEntity;

public interface JudgeMilestoneRepository extends BaseRepository<JudgeMilestoneEntity> {

    Optional<JudgeMilestoneEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);
}
