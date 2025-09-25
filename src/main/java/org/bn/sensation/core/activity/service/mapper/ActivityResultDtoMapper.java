package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityResultEntity;
import org.bn.sensation.core.activity.service.dto.ActivityResultDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface ActivityResultDtoMapper extends BaseDtoMapper<ActivityResultEntity, ActivityResultDto> {

    @Override
    ActivityResultEntity toEntity(ActivityResultDto dto);

    @Override
    ActivityResultDto toDto(ActivityResultEntity entity);
}
