package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface UserActivityAssignmentDtoMapper extends BaseDtoMapper<UserActivityAssignmentEntity, UserActivityAssignmentDto> {

    @Override
    UserActivityAssignmentEntity toEntity(UserActivityAssignmentDto dto);

    @Override
    UserActivityAssignmentDto toDto(UserActivityAssignmentEntity entity);
}
