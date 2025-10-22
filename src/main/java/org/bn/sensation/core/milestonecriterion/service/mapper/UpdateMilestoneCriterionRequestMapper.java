package org.bn.sensation.core.milestonecriterion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.service.dto.UpdateMilestoneCriterionRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneCriterionRequestMapper extends BaseDtoMapper<MilestoneCriterionEntity, UpdateMilestoneCriterionRequest> {

    @Override
    MilestoneCriterionEntity toEntity(UpdateMilestoneCriterionRequest dto);

    UpdateMilestoneCriterionRequest toDto(MilestoneCriterionEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMilestoneCriterionAssignmentFromRequest(UpdateMilestoneCriterionRequest request, @MappingTarget MilestoneCriterionEntity entity);
}
