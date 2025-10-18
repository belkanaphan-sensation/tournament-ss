package org.bn.sensation.core.useractivity.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.service.dto.UpdateUserActivityAssignmentRequest;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateUserActivityAssignmentRequestMapper extends BaseDtoMapper<UserActivityAssignmentEntity, UpdateUserActivityAssignmentRequest> {

    @Override
    UserActivityAssignmentEntity toEntity(UpdateUserActivityAssignmentRequest dto);

    @Override
    UpdateUserActivityAssignmentRequest toDto(UserActivityAssignmentEntity entity);
}
