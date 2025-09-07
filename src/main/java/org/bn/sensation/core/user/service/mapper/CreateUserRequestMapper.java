package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.CreateUserRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = BaseDtoMapper.class)
public interface CreateUserRequestMapper extends BaseDtoMapper<UserEntity, CreateUserRequest> {
    @Override
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(CreateUserRequest dto);

    @Override
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "roleIds", source = "roles")
    CreateUserRequest toDto(UserEntity entity);

    default Set<Long> map(Set<RoleEntity> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toSet());
    }
}
