package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRuleRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneRuleRequestMapper extends BaseDtoMapper<MilestoneRuleEntity, UpdateMilestoneRuleRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMilestoneRuleFromRequest(UpdateMilestoneRuleRequest request, @MappingTarget MilestoneRuleEntity entity);
}
