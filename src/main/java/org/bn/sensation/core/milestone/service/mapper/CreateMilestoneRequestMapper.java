package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneRequestMapper extends BaseDtoMapper<MilestoneEntity, CreateMilestoneRequest> {
    @Override
    @Mapping(target = "activity", ignore = true)
    MilestoneEntity toEntity(CreateMilestoneRequest dto);
}
