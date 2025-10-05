package org.bn.sensation.core.round.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.JudgeRoundEntity;

public interface JudgeRoundRepository extends BaseRepository<JudgeRoundEntity> {

    Optional<JudgeRoundEntity> findByRoundIdAndJudgeId(Long roundId, Long judgeId);
}
