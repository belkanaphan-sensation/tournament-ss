package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.bn.sensation.security.SecurityUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface UserDtoMapper extends BaseDtoMapper<UserEntity, UserDto> {
    @Override
    UserEntity toEntity(UserDto dto);

    @Override
    @Mapping(target = "organizations", source = "organizations")
    UserDto toDto(UserEntity entity);

    UserDto toDto(SecurityUser securityUser);
}
