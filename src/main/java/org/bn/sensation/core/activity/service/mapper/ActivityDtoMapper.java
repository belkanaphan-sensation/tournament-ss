package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.occasion.service.mapper.OccasionDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {OccasionDtoMapper.class})
public interface ActivityDtoMapper extends BaseDtoMapper<ActivityEntity, ActivityDto> {

    @Override
    ActivityEntity toEntity(ActivityDto dto);

    @Override
    ActivityDto toDto(ActivityEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(ActivityEntity entity);
}
