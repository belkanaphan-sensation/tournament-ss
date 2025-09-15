package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.UpdateUserRequest;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateUserRequestMapper extends BaseDtoMapper<UserEntity, UpdateUserRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget UserEntity entity);

}
