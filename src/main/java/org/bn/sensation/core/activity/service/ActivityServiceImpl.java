package org.bn.sensation.core.activity.service;

import org.bn.sensation.common.service.BaseService;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.mapper.ActivityDtoMapper;

public class ActivityServiceImpl extends BaseService<ActivityEntity, ActivityDto>
        implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;

    public ActivityServiceImpl(
            ActivityRepository activityRepository, ActivityDtoMapper activityDtoMapper) {
        super(activityRepository, activityDtoMapper);
        this.activityRepository = activityRepository;
        this.activityDtoMapper = activityDtoMapper;
    }
}
