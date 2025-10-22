package org.bn.sensation.core.judgemilestonestatus.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatusEntity;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface JudgeMilestoneStatusRepository extends BaseRepository<JudgeMilestoneStatusEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<JudgeMilestoneStatusEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<JudgeMilestoneStatusEntity> findByMilestoneId(Long milestoneId);
}
