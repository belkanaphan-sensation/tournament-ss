package org.bn.sensation.core.useractivity.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.service.dto.CreateUserActivityAssignmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateUserActivityAssignmentRequestMapper extends BaseDtoMapper<UserActivityAssignmentEntity, CreateUserActivityAssignmentRequest> {

    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "activity", ignore = true)
    UserActivityAssignmentEntity toEntity(CreateUserActivityAssignmentRequest dto);
}
