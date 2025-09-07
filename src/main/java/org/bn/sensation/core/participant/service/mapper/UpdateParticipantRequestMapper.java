package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateParticipantRequestMapper extends BaseDtoMapper<ParticipantEntity, UpdateParticipantRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateParticipantFromRequest(UpdateParticipantRequest request, @MappingTarget ParticipantEntity entity);

    default ActivityEntity map(Long activityId) {
        if (activityId == null) {
            return null;
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setId(activityId);
        return activity;
    }
}
