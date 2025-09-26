package org.bn.sensation.core.round.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoundResultRepository extends BaseRepository<RoundResultEntity> {

    List<RoundResultEntity> findByRoundId(Long roundId);

    List<RoundResultEntity> findByParticipantId(Long participantId);

    List<RoundResultEntity> findByActivityUserId(Long activityUserId);

    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT rr
            FROM RoundResultEntity rr
            WHERE rr.round.milestone.id = :milestoneId
            """)
    List<RoundResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    boolean existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
            Long roundId, Long participantId, Long activityUserId, Long milestoneCriteriaId);
}
