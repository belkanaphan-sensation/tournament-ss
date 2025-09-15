package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneRequestMapper extends BaseDtoMapper<MilestoneEntity, CreateMilestoneRequest> {
    @Override
    @Mapping(target = "activity", source = "activityId")
    MilestoneEntity toEntity(CreateMilestoneRequest dto);

    @Override
    @Mapping(target = "activityId", source = "activity.id")
    CreateMilestoneRequest toDto(MilestoneEntity entity);

    default ActivityEntity map(Long activityId) {
        if (activityId == null) {
            return null;
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setId(activityId);
        return activity;
    }
}
