package org.bn.sensation.core.activity.repository;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface ActivityRepository extends BaseRepository<ActivityEntity> {

    List<ActivityEntity> findByOccasionId(Long occasionId);

    @EntityGraph(attributePaths = {"occasion", "milestones"})
    @Query("""
            SELECT DISTINCT a
            FROM ActivityEntity a
            JOIN UserActivityAssignmentEntity uaa ON uaa.activity.id = a.id
            WHERE a.occasion.id = :occasionId
              AND uaa.user.id = :userId
              AND a.state IN :states
            """)
    List<ActivityEntity> findByOccasionIdAndUserIdAndStateIn(Long occasionId, Long userId, List<ActivityState> states);

}
