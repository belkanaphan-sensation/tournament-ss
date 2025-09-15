package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateUserActivityAssignmentRequestMapper extends BaseDtoMapper<UserActivityAssignmentEntity, UpdateUserActivityAssignmentRequest> {

    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "activity", ignore = true)
    UserActivityAssignmentEntity toEntity(UpdateUserActivityAssignmentRequest dto);

    @Override
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "activityId", source = "activity.id")
    UpdateUserActivityAssignmentRequest toDto(UserActivityAssignmentEntity entity);
}
