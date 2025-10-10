package org.bn.sensation.core.milestone.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JudgeMilestoneResultRepository extends BaseRepository<JudgeMilestoneResultEntity> {

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

    boolean existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
            Long roundId, Long participantId, Long activityUserId, Long milestoneCriteriaId);
}
