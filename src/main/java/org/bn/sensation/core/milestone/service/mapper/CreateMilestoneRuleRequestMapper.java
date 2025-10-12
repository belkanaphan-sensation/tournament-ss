package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneRuleRequestMapper extends BaseDtoMapper<MilestoneRuleEntity, CreateMilestoneRuleRequest> {

    @Override
    @Mapping(target = "milestone", ignore = true)
    MilestoneRuleEntity toEntity(CreateMilestoneRuleRequest dto);

}
