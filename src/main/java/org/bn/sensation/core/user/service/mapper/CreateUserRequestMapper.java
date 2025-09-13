package org.bn.sensation.core.user.service.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateUserRequestMapper extends BaseDtoMapper<UserEntity, CreateUserRequest> {
    @Override
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    UserEntity toEntity(CreateUserRequest dto);

    @Override
    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "surname", source = "person.surname")
    @Mapping(target = "secondName", source = "person.secondName")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "phoneNumber", source = "person.phoneNumber")
    @Mapping(target = "roleIds", source = "roles")
    @Mapping(target = "organizationIds", source = "organizations")
    CreateUserRequest toDto(UserEntity entity);

    default Set<Long> mapRoles(Set<RoleEntity> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toSet());
    }

    default Set<Long> mapOrganizations(Set<OrganizationEntity> organizations) {
        if (organizations == null) {
            return null;
        }
        return organizations.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toSet());
    }
}
