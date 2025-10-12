package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateUserRequestMapper extends BaseDtoMapper<UserEntity, CreateUserRequest> {
    @Override
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    UserEntity toEntity(CreateUserRequest dto);

}
