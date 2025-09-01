package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface UserDtoMapper extends BaseDtoMapper<UserEntity, UserDto> {
    @Override
    UserEntity toEntity(UserDto dto);

    @Override
    UserDto toDto(UserEntity entity);
}
