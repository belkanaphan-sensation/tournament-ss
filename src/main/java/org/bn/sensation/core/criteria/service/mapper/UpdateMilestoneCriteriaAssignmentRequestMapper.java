package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneCriteriaAssignmentRequestMapper extends BaseDtoMapper<MilestoneCriteriaAssignmentEntity, UpdateMilestoneCriteriaAssignmentRequest> {

    @Override
    MilestoneCriteriaAssignmentEntity toEntity(UpdateMilestoneCriteriaAssignmentRequest dto);

    UpdateMilestoneCriteriaAssignmentRequest toDto(MilestoneCriteriaAssignmentEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMilestoneCriteriaAssignmentFromRequest(UpdateMilestoneCriteriaAssignmentRequest request, @MappingTarget MilestoneCriteriaAssignmentEntity entity);
}
