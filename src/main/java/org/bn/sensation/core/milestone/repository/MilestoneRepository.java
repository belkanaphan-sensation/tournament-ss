package org.bn.sensation.core.milestone.repository;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilestoneRepository extends BaseRepository<MilestoneEntity> {

    List<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId);

    List<MilestoneEntity> findByActivityIdAndStateInOrderByMilestoneOrderAsc(@Param("activityId") Long activityId, @Param("states") List<State> states);

    @EntityGraph(attributePaths = {"activity.userAssignments.user", "rounds.participants"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.id = :id")
    Optional<MilestoneEntity> findByIdWithUserAssignments(@Param("id") Long id);

    @EntityGraph(attributePaths = {"criteriaAssignments"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.id = :id")
    Optional<MilestoneEntity> findByIdWithCriteriaAssignments(@Param("id") Long id);

    @EntityGraph(attributePaths = {"criteriaAssignments"})
    @Query("SELECT m FROM MilestoneEntity m")
    List<MilestoneEntity> findAllWithCriteriaAssignments();

    @EntityGraph(attributePaths = {"criteriaAssignments"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.activity.id = :activityId ORDER BY m.milestoneOrder ASC")
    List<MilestoneEntity> findByActivityIdWithCriteriaAssignments(@Param("activityId") Long activityId);

    @EntityGraph(attributePaths = {"criteriaAssignments"})
    @Query("SELECT m FROM MilestoneEntity m WHERE m.activity.id = :activityId AND m.state IN :states ORDER BY m.milestoneOrder ASC")
    List<MilestoneEntity> findByActivityIdAndStateInWithCriteriaAssignments(@Param("activityId") Long activityId, @Param("states") List<State> states);

}
