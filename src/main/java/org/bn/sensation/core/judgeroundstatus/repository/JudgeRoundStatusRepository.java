package org.bn.sensation.core.judgeroundstatus.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;

public interface JudgeRoundStatusRepository extends BaseRepository<JudgeRoundStatusEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT jr FROM JudgeRoundStatusEntity jr WHERE jr.round.id = :roundId AND jr.judge.id = :judgeId")
    Optional<JudgeRoundStatusEntity> findByRoundIdAndJudgeId(@Param("roundId") Long roundId, @Param("judgeId") Long judgeId);

    default JudgeRoundStatusEntity getByRoundIdAndJudgeIdOrThrow(Long roundId, Long judgeId) {
        return findByRoundIdAndJudgeId(roundId, judgeId).orElseThrow(() ->
                new EntityNotFoundException("Не найден статус для раунда %s и судьи %s".formatted(roundId, judgeId)));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT jr FROM JudgeRoundStatusEntity jr WHERE jr.round.id = :roundId")
    List<JudgeRoundStatusEntity> findByRoundId(@Param("roundId") Long roundId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    int countByJudgeIdAndStatusAndRoundIdIn(Long judgeId, JudgeRoundStatus status, List<Long> roundIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"round"})
    @Query("SELECT DISTINCT jr FROM JudgeRoundStatusEntity jr WHERE jr.round.milestone.id = :milestoneId and jr.judge.id = :judgeId")
    List<JudgeRoundStatusEntity> findByMilestoneIdAndJudgeId(@Param("milestoneId") Long milestoneId, @Param("judgeId") Long judgeId);

    @Query("""
            SELECT count(jrs) FROM JudgeRoundStatusEntity jrs
            JOIN jrs.round r
            WHERE r.milestone.id = :milestoneId and jrs.judge.id = :judgeId and jrs.status <> :status
            """)
    Long countNotEqualStatusForMilestoneIdAndJudgeId(
            @Param("milestoneId") Long milestoneId,
            @Param("judgeId") Long judgeId,
            @Param("status") JudgeRoundStatus status);
}
