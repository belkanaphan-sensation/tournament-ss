package org.bn.sensation.core.judge.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.judge.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judge.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface JudgeMilestoneResultMilestoneRequestMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultMilestoneRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateJudgeMilestoneResultMilestoneFromRequest(JudgeMilestoneResultMilestoneRequest request, @MappingTarget JudgeMilestoneResultEntity entity);

}
