package org.bn.sensation.core.role.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.role.service.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService extends BaseService<RoleEntity, RoleDto> {

    // CRUD operations
    Page<RoleDto> findAll(Pageable pageable);

    RoleDto create(CreateRoleRequest request);

    void deleteById(Long id);
}
