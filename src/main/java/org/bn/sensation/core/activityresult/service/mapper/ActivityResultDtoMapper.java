package org.bn.sensation.core.activityresult.service.mapper;

import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
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
