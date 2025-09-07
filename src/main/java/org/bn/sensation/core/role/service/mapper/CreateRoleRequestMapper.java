package org.bn.sensation.core.role.service.mapper;

import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface CreateRoleRequestMapper extends BaseDtoMapper<RoleEntity, CreateRoleRequest> {
}
