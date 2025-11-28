package org.bn.sensation.core.judgemilestoneresult.repository;

import java.util.Collection;
import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JudgeMilestoneResultRepository extends BaseRepository<JudgeMilestoneResultEntity> {

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
            """)
    List<JudgeMilestoneResultEntity> findByRoundId(Long roundId);

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
                        AND jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByRoundIdAndActivityUserId(Long roundId, Long activityUserId);

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.contestant.id = :contestantId
            """)
    List<JudgeMilestoneResultEntity> findByContestantId(Long contestantId);

    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.contestant.id in :contestantIds
              AND jm.round.milestone.id = :milestoneId
            """)
    List<JudgeMilestoneResultEntity> findByContestantIdAndMilestoneId(@Param("contestantIds") Collection<Long> contestantIds, @Param("milestoneId") Long milestoneId);

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByActivityUserId(Long activityUserId);

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.milestone.id = :milestoneId
            """)
    List<JudgeMilestoneResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    @EntityGraph(attributePaths = {"contestant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.milestone.id = :milestoneId
                        AND jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByMilestoneIdAndActivityUserId(Long milestoneId, Long activityUserId);

    @EntityGraph(attributePaths = {"milestoneCriterion"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.id in :ids
            """)
    List<JudgeMilestoneResultEntity> findByIdsIn(@Param("ids") List<Long> ids);

    boolean existsByRoundIdAndContestantIdAndActivityUserIdAndMilestoneCriterionId(
            Long roundId, Long contestantId, Long activityUserId, Long milestoneCriterionId);
}
