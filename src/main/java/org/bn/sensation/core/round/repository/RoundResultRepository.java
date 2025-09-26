package org.bn.sensation.core.round.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoundResultRepository extends BaseRepository<RoundResultEntity> {

    /**
     * Найти результаты раунда по ID раунда
     */
    List<RoundResultEntity> findByRoundId(Long roundId);

    /**
     * Найти результаты раунда по ID участника
     */
    List<RoundResultEntity> findByParticipantId(Long participantId);

    /**
     * Найти результаты раунда по ID судьи
     */
    List<RoundResultEntity> findByActivityUserId(Long activityUserId);

    /**
     * Найти результаты раунда по ID этапа через связь с раундом
     */
    @EntityGraph(attributePaths = {"participant", "round", "milestoneCriteria", "activityUser"})
    @Query("""
            SELECT DISTINCT rr 
            FROM RoundResultEntity rr
            WHERE rr.round.milestone.id = :milestoneId
            """)
    List<RoundResultEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);
}
