package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateActivityRequestMapper extends BaseDtoMapper<ActivityEntity, CreateActivityRequest> {
    @Override
    @Mapping(target = "occasion", source = "occasionId")
    ActivityEntity toEntity(CreateActivityRequest dto);

    @Override
    @Mapping(target = "occasionId", source = "occasion.id")
    CreateActivityRequest toDto(ActivityEntity entity);

    default OccasionEntity map(Long occasionId) {
        if (occasionId == null) {
            return null;
        }
        OccasionEntity occasion = new OccasionEntity();
        occasion.setId(occasionId);
        return occasion;
    }
}
