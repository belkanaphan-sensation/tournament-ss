package org.bn.sensation.core.judgemilestoneresult.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;

public interface JudgeMilestoneResultRepository extends BaseRepository<JudgeMilestoneResultEntity> {

    @EntityGraph(attributePaths = {"activityUser.user", "round.milestone"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.id = :id
            """)
    Optional<JudgeMilestoneResultEntity> findByIdWithUserAndMilestone(Long id);

    default JudgeMilestoneResultEntity getByIdWithUserAndMilestoneOrThrow(Long id) {
        return findByIdWithUserAndMilestone(id).orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден: " + id));
    }

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
            """)
    List<JudgeMilestoneResultEntity> findByRoundId(Long roundId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.id = :roundId
                        AND jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByRoundIdAndActivityUserId(Long roundId, Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.participant.id = :participantId
            """)
    List<JudgeMilestoneResultEntity> findByParticipantId(Long participantId);

    @EntityGraph(attributePaths = {"participant.activity", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.activityUser.id = :activityUserId
            """)
    List<JudgeMilestoneResultEntity> findByActivityUserId(Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriterion", "activityUser"})
    @Query("""
            SELECT DISTINCT jm
            FROM JudgeMilestoneResultEntity jm
            WHERE jm.round.milestone.id = :milestoneId
            """)
    List<JudgeMilestoneResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriterion", "activityUser"})
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

    boolean existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriterionId(
            Long roundId, Long participantId, Long activityUserId, Long milestoneCriterionId);
}
