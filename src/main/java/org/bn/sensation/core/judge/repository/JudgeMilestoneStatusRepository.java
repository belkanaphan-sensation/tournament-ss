package org.bn.sensation.core.judge.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatusEntity;

public interface JudgeMilestoneStatusRepository extends BaseRepository<JudgeMilestoneStatusEntity> {

    Optional<JudgeMilestoneStatusEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);

    List<JudgeMilestoneStatusEntity> findByMilestoneId(Long milestoneId);
}
