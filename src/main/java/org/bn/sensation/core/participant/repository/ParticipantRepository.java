package org.bn.sensation.core.participant.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;

public interface ParticipantRepository extends BaseRepository<ParticipantEntity> {

    Long countByMilestones_IdAndPartnerSide(@Param("milestoneId") Long milestoneId, @Param("partnerSide") PartnerSide partnerSide);

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("""
            SELECT DISTINCT p
            FROM ParticipantEntity p
            JOIN p.rounds r
            WHERE r.id = :roundId
            """)
    List<ParticipantEntity> findByRoundId(@Param("roundId") Long roundId);

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.activity.id = :activityId")
    List<ParticipantEntity> findByActivityId(@Param("activityId") Long activityId);

    List<ParticipantEntity> findByActivityIdAndIdIn(@Param("activityId") Long activityId, @Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id IN :ids")
    List<ParticipantEntity> findAllByIdFull(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"activity"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id IN :ids")
    List<ParticipantEntity> findAllByIdWithActivity(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id = :id")
    Optional<ParticipantEntity> findByIdFull(@Param("id") Long id);

    @Query("""
            SELECT COUNT(p) > 0
            FROM ParticipantEntity p
            JOIN p.rounds r
            WHERE p.id = :participantId
            AND r.milestone.id = :milestoneId
            """)
    boolean existsByParticipantIdAndMilestoneId(@Param("participantId") Long participantId, @Param("milestoneId") Long milestoneId);

    default ParticipantEntity getByIdFullOrThrow(Long id) {
        return findByIdFull(id).orElseThrow(() -> new EntityNotFoundException("Участник не найден: " + id));
    }

    default ParticipantEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Участник не найден: " + id));
    }
}
