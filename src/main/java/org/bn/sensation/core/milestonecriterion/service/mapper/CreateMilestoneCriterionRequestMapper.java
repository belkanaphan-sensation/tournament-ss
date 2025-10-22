package org.bn.sensation.core.milestonecriterion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.service.dto.CreateMilestoneCriterionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneCriterionRequestMapper extends BaseDtoMapper<MilestoneCriterionEntity, CreateMilestoneCriterionRequest> {

    @Override
    @Mapping(target = "milestoneRule", ignore = true)
    @Mapping(target = "criterion", ignore = true)
    MilestoneCriterionEntity toEntity(CreateMilestoneCriterionRequest dto);
}
