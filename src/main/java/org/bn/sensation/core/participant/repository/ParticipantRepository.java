package org.bn.sensation.core.participant.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends BaseRepository<ParticipantEntity> {

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("""
            SELECT DISTINCT p
            FROM ParticipantEntity p
            JOIN p.rounds r
            WHERE r.id = :roundId
            """)
    List<ParticipantEntity> findByRoundId(@Param("roundId") Long roundId);

//    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
//    @Query("""
//            SELECT DISTINCT p
//            FROM ParticipantEntity p
//            JOIN p.milestones m
//            WHERE m.id = :milestoneId
//            """)
//    List<ParticipantEntity> findByMilestoneId(@Param("milestoneId") Long milestoneId);

    @EntityGraph(attributePaths = {"activity"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id IN :ids")
    List<ParticipantEntity> findAllByIdWithActivity(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"activity"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id = :id")
    Optional<ParticipantEntity> findByIdWithActivity(@Param("id") Long id);

    @EntityGraph(attributePaths = {"activity", "rounds", "milestones"})
    @Query("SELECT p FROM ParticipantEntity p WHERE p.id = :id")
    Optional<ParticipantEntity> findByIdFullEntity(@Param("id") Long id);
}
