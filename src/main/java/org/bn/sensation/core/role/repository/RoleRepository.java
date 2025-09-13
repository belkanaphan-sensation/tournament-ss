package org.bn.sensation.core.role.repository;


import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;

public interface RoleRepository extends BaseRepository<RoleEntity> {

    Optional<RoleEntity> findByRole(Role role);
}
