package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneCriteriaAssignmentRequestMapper extends BaseDtoMapper<MilestoneCriteriaAssignmentEntity, UpdateMilestoneCriteriaAssignmentRequest> {

    @Override
    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    MilestoneCriteriaAssignmentEntity toEntity(UpdateMilestoneCriteriaAssignmentRequest dto);

    @Override
    @Mapping(target = "milestoneId", source = "milestone.id")
    @Mapping(target = "criteriaId", source = "criteria.id")
    UpdateMilestoneCriteriaAssignmentRequest toDto(MilestoneCriteriaAssignmentEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    void updateMilestoneCriteriaAssignmentFromRequest(UpdateMilestoneCriteriaAssignmentRequest request, @MappingTarget MilestoneCriteriaAssignmentEntity entity);
}
