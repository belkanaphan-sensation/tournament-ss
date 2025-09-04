package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;
import org.bn.sensation.core.role.service.mapper.RoleDtoMapper;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {OrganizationDtoMapper.class, RoleDtoMapper.class})
public interface UserDtoMapper extends BaseDtoMapper<UserEntity, UserDto> {
    @Override
    UserEntity toEntity(UserDto dto);

    @Override
    UserDto toDto(UserEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "username")
    EntityLinkDto toEntityLinkDto(UserEntity entity);
}
