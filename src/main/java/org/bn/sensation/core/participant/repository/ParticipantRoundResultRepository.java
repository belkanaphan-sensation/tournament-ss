package org.bn.sensation.core.participant.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRoundResultRepository extends BaseRepository<ParticipantRoundResultEntity> {

    List<ParticipantRoundResultEntity> findByRoundId(Long roundId);

    List<ParticipantRoundResultEntity> findByParticipantId(Long participantId);

    List<ParticipantRoundResultEntity> findByActivityUserId(Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT rr
            FROM ParticipantRoundResultEntity rr
            WHERE rr.round.milestone.id = :milestoneId
            """)
    List<ParticipantRoundResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    boolean existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
            Long roundId, Long participantId, Long activityUserId, Long milestoneCriteriaId);
}
