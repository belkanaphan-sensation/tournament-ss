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
    @Mapping(target = "occasion", ignore = true)
    ActivityEntity toEntity(CreateActivityRequest dto);


}
