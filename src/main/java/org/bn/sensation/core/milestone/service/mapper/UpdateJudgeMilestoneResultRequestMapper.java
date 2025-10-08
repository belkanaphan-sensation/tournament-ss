package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.UpdateJudgeMilestoneResultRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateJudgeMilestoneResultRequestMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, UpdateJudgeMilestoneResultRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoundFromRequest(UpdateJudgeMilestoneResultRequest request, @MappingTarget JudgeMilestoneResultEntity entity);

}
