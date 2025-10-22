package org.bn.sensation.core.activityuser.service.mapper;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.activityuser.service.dto.CreateActivityUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateActivityUserRequestMapper extends BaseDtoMapper<ActivityUserEntity, CreateActivityUserRequest> {

    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "activity", ignore = true)
    ActivityUserEntity toEntity(CreateActivityUserRequest dto);
}
