package org.bn.sensation.core.role.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.service.dto.RoleDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface RoleDtoMapper extends BaseDtoMapper<RoleEntity, RoleDto> {

    @Override
    RoleEntity toEntity(RoleDto dto);

    @Override
    RoleDto toDto(RoleEntity entity);
}
