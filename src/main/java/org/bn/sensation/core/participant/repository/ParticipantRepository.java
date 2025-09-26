package org.bn.sensation.core.participant.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends BaseRepository<ParticipantEntity> {

    @EntityGraph(attributePaths = {"rounds", "milestones", "activity"})
    @Query("""
            SELECT DISTINCT p 
            FROM ParticipantEntity p
            JOIN p.rounds r
            WHERE r.id = :roundId
            """)
    List<ParticipantEntity> findByRoundId(@Param("roundId") Long roundId);
}
