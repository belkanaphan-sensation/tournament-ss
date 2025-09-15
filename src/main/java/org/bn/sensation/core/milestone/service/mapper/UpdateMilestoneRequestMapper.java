package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneRequestMapper extends BaseDtoMapper<MilestoneEntity, UpdateMilestoneRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMilestoneFromRequest(UpdateMilestoneRequest request, @MappingTarget MilestoneEntity entity);

    default ActivityEntity map(Long activityId) {
        if (activityId == null) {
            return null;
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setId(activityId);
        return activity;
    }
}
