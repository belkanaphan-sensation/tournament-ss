package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface UserActivityAssignmentDtoMapper extends BaseDtoMapper<UserActivityAssignmentEntity, UserActivityAssignmentDto> {
    
    @Override
    UserActivityAssignmentEntity toEntity(UserActivityAssignmentDto dto);

    @Override
    @Mapping(target = "user", source = "user")
    @Mapping(target = "activity", source = "activity")
    @Mapping(target = "userName", source = "user.person.name")
    @Mapping(target = "userSurname", source = "user.person.surname")
    @Mapping(target = "userSecondName", source = "user.person.secondName")
    UserActivityAssignmentDto toDto(UserActivityAssignmentEntity entity);
}
