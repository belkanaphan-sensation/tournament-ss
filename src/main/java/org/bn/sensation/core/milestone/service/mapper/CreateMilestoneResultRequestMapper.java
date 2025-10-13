package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneResultRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneResultRequestMapper extends BaseDtoMapper<MilestoneResultEntity, CreateMilestoneResultRequest> {

    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "participant", ignore = true)
//    @Mapping(target = "round", ignore = true)
    @Override
    MilestoneResultEntity toEntity(CreateMilestoneResultRequest request);

}
