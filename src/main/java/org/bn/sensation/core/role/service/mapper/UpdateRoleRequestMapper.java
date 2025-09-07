package org.bn.sensation.core.role.service.mapper;

import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.service.dto.UpdateRoleRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateRoleRequestMapper extends BaseDtoMapper<RoleEntity, UpdateRoleRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromRequest(UpdateRoleRequest request, @MappingTarget RoleEntity entity);
}
