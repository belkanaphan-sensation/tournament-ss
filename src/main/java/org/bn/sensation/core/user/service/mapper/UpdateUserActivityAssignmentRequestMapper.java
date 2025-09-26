package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateUserActivityAssignmentRequestMapper extends BaseDtoMapper<UserActivityAssignmentEntity, UpdateUserActivityAssignmentRequest> {

    @Override
    UserActivityAssignmentEntity toEntity(UpdateUserActivityAssignmentRequest dto);

    @Override
    UpdateUserActivityAssignmentRequest toDto(UserActivityAssignmentEntity entity);
}
