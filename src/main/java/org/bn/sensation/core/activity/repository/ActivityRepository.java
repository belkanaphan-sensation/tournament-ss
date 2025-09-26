package org.bn.sensation.core.activity.repository;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;

public interface ActivityRepository extends BaseRepository<ActivityEntity> {

    List<ActivityEntity> findByOccasionId(Long occasionId);

    List<ActivityEntity> findByOccasionIdAndStateIn(Long occasionId, List<State> states);

}
