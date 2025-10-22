package org.bn.sensation.core.activityuser.service.mapper;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.activityuser.service.dto.ActivityUserDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface ActivityUserDtoMapper extends BaseDtoMapper<ActivityUserEntity, ActivityUserDto> {

    @Override
    ActivityUserEntity toEntity(ActivityUserDto dto);

    @Override
    ActivityUserDto toDto(ActivityUserEntity entity);
}
