package org.bn.sensation.core.judge.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeRoundEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface JudgeRoundRepository extends BaseRepository<JudgeRoundEntity> {

    Optional<JudgeRoundEntity> findByRoundIdAndJudgeId(Long roundId, Long judgeId);

    List<JudgeRoundEntity> findByRoundId(Long roundId);

    int countByJudgeIdAndStatusAndRoundIdIn(Long judgeId, JudgeRoundStatus status, List<Long> roundIds);

    @EntityGraph(attributePaths = {"round"})
    @Query("SELECT jr FROM JudgeRoundEntity jr WHERE jr.round.milestone.id = :milestoneId and jr.judge.id = :judgeId")
    List<JudgeRoundEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);
}
