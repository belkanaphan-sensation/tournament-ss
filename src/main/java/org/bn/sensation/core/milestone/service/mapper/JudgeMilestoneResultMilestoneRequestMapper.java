package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface JudgeMilestoneResultMilestoneRequestMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultMilestoneRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoundFromRequest(JudgeMilestoneResultMilestoneRequest request, @MappingTarget JudgeMilestoneResultEntity entity);

}
