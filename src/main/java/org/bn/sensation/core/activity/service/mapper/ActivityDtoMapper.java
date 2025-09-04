package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface ActivityDtoMapper extends BaseDtoMapper<ActivityEntity, ActivityDto> {

    @Override
    ActivityEntity toEntity(ActivityDto dto);

    @Override
    ActivityDto toDto(ActivityEntity entity);
}
