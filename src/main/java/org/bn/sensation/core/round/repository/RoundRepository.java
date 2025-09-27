package org.bn.sensation.core.round.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoundRepository extends BaseRepository<RoundEntity> {

    Page<RoundEntity> findByMilestoneId(Long milestoneId, Pageable pageable);

    List<RoundEntity> findByMilestoneId(Long milestoneId);

    List<RoundEntity> findByMilestoneIdAndStateIn(@Param("milestoneId") Long milestoneId, @Param("states") List<State> states);

    @EntityGraph(attributePaths = {"milestone.activity.userAssignments.user"})
    @Query("SELECT r FROM RoundEntity r WHERE r.id = :id")
    Optional<RoundEntity> findByIdWithUserAssignments(@Param("id") Long id);
}
