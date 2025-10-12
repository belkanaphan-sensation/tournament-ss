package org.bn.sensation.core.judge.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeMilestoneEntity;

public interface JudgeMilestoneRepository extends BaseRepository<JudgeMilestoneEntity> {

    Optional<JudgeMilestoneEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);

    List<JudgeMilestoneEntity> findByMilestoneId(Long milestoneId);
}
