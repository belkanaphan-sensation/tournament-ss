package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneCriteriaAssignmentRequestMapper extends BaseDtoMapper<MilestoneCriteriaAssignmentEntity, CreateMilestoneCriteriaAssignmentRequest> {

    @Override
    @Mapping(target = "milestoneRule", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    MilestoneCriteriaAssignmentEntity toEntity(CreateMilestoneCriteriaAssignmentRequest dto);
}
