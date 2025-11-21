package org.bn.sensation.core.contestant.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;

public interface ContestantRepository extends BaseRepository<ContestantEntity> {

    @EntityGraph(attributePaths = {"rounds", "milestones", "participants"})
    @Query("""
            SELECT DISTINCT c
            FROM ContestantEntity c
            JOIN c.rounds r
            WHERE r.id = :roundId
            """)
    List<ContestantEntity> findByRoundId(@Param("roundId") Long roundId);

    @EntityGraph(attributePaths = {"rounds", "milestones", "participants"})
    @Query("SELECT c FROM ContestantEntity c WHERE c.id IN :ids")
    List<ContestantEntity> findAllByIdFull(@Param("ids") List<Long> ids);

    default ContestantEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Конкурсант не найден по Id: " + id));
    }
}
