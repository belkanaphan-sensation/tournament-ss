package org.bn.sensation.core.role.repository;


import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;

public interface RoleRepository extends BaseRepository<RoleEntity> {

    RoleEntity findByRole(Role role);
}
