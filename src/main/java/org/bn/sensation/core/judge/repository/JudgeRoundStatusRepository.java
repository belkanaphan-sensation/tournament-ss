package org.bn.sensation.core.judge.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface JudgeRoundStatusRepository extends BaseRepository<JudgeRoundStatusEntity> {

    Optional<JudgeRoundStatusEntity> findByRoundIdAndJudgeId(Long roundId, Long judgeId);

    List<JudgeRoundStatusEntity> findByRoundId(Long roundId);

    int countByJudgeIdAndStatusAndRoundIdIn(Long judgeId, JudgeRoundStatus status, List<Long> roundIds);

    @EntityGraph(attributePaths = {"round"})
    @Query("SELECT jr FROM JudgeRoundStatusEntity jr WHERE jr.round.milestone.id = :milestoneId and jr.judge.id = :judgeId")
    List<JudgeRoundStatusEntity> findByMilestoneIdAndJudgeId(Long milestoneId, Long judgeId);
}
