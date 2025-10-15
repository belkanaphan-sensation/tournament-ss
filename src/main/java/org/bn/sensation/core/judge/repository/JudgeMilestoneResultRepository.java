package org.bn.sensation.core.judge.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeMilestoneResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JudgeMilestoneResultRepository extends BaseRepository<JudgeMilestoneResultEntity> {

    @EntityGraph(attributePaths = {"activityUser.user"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.id = :id
            """)
    Optional<JudgeMilestoneResultEntity> findByIdWithUser(Long id);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
            """)
    List<JudgeMilestoneResultEntity> findByRoundId(Long roundId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
                        AND jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByRoundIdAndActivityUserId(Long roundId, Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.participant.id = :participantId
            """)
    List<JudgeMilestoneResultEntity> findByParticipantId(Long participantId);

    @EntityGraph(attributePaths = {"participant.activity", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByActivityUserId(Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.milestone.id = :milestoneId
            """)
    List<JudgeMilestoneResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.milestone.id = :milestoneId
                        AND jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByMilestoneIdAndActivityUserId(Long milestoneId, Long activityUserId);

    @EntityGraph(attributePaths = {"milestoneCriteria"})
    @Query("""
            SELECT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.id in :ids
            """)
    List<JudgeMilestoneResultEntity> findByIdsIn(@Param("ids") List<Long> ids);

    boolean existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
            Long roundId, Long participantId, Long activityUserId, Long milestoneCriteriaId);
}
