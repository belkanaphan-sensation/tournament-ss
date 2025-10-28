package org.bn.sensation.core.milestone.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityNotFoundException;

public interface MilestoneRepository extends BaseRepository<MilestoneEntity> {

    List<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId);

    @EntityGraph(attributePaths = {"activity.activityUsers.user", "rounds.participants", "milestoneRule", "participants", "results"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.id = :id")
    Optional<MilestoneEntity> findByIdFull(@Param("id") Long id);

    default MilestoneEntity getByIdFullOrThrow(Long id) {
        return findByIdFull(id).orElseThrow(() -> new EntityNotFoundException("Этап не найден: " + id));
    }

    @EntityGraph(attributePaths = {"activity.activityUsers.user"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.id = :id")
    Optional<MilestoneEntity> findByIdWithActivityUsers(@Param("id") Long id);

    default MilestoneEntity getByIdWithActivityUsersOrThrow(Long id) {
        return findByIdWithActivityUsers(id).orElseThrow(() -> new EntityNotFoundException("Этап не найден: " + id));
    }

    @EntityGraph(attributePaths = {"activity", "milestoneRule"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.activity.id = :activityId AND m.state IN :states")
    List<MilestoneEntity> findByActivityIdAndStateIn(@Param("activityId") Long activityId, @Param("states") List<MilestoneState> states);

    @Query("SELECT m.milestoneRule.participantLimit FROM MilestoneEntity m " +
            "WHERE m.milestoneOrder = :milestoneOrder " +
            "AND m.activity.id = :activityId")
    Integer getParticipantLimitForNextMilestone(Long activityId, Integer milestoneOrder);

    default MilestoneEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Этап не найден: " + id));
    }
}
