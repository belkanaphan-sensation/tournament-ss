package org.bn.sensation.core.participant.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends BaseRepository<ParticipantEntity> {

    @Query("SELECT p FROM ParticipantEntity p " +
           "JOIN p.rounds r " +
           "WHERE r.id = :roundId")
    Page<ParticipantEntity> findByRoundId(@Param("roundId") Long roundId, Pageable pageable);
}
