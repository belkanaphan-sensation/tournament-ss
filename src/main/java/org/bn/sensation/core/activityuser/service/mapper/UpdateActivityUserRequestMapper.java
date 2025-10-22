package org.bn.sensation.core.activityuser.service.mapper;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.activityuser.service.dto.UpdateActivityUserRequest;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateActivityUserRequestMapper extends BaseDtoMapper<ActivityUserEntity, UpdateActivityUserRequest> {

    @Override
    ActivityUserEntity toEntity(UpdateActivityUserRequest dto);

    @Override
    UpdateActivityUserRequest toDto(ActivityUserEntity entity);
}
