package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneCriteriaAssignmentRequestMapper extends BaseDtoMapper<MilestoneCriteriaAssignmentEntity, CreateMilestoneCriteriaAssignmentRequest> {

    @Override
    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    MilestoneCriteriaAssignmentEntity toEntity(CreateMilestoneCriteriaAssignmentRequest dto);

    @Override
    @Mapping(target = "milestoneId", source = "milestone.id")
    @Mapping(target = "criteriaId", source = "criteria.id")
    CreateMilestoneCriteriaAssignmentRequest toDto(MilestoneCriteriaAssignmentEntity entity);
}
