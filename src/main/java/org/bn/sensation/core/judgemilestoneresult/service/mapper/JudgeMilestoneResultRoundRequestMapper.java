package org.bn.sensation.core.judgemilestoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface JudgeMilestoneResultRoundRequestMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultRoundRequest> {

    @Override
    @Mapping(target = "contestant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriterion", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    JudgeMilestoneResultEntity toEntity(JudgeMilestoneResultRoundRequest dto);

    @Override
    @Mapping(target = "contestantId", source = "contestant.id")
    @Mapping(target = "roundId", source = "round.id")
    @Mapping(target = "milestoneCriterionId", source = "milestoneCriterion.id")
    JudgeMilestoneResultRoundRequest toDto(JudgeMilestoneResultEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contestant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriterion", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    void updateJudgeMilestoneResultRoundFromRequest(JudgeMilestoneResultRoundRequest request, @MappingTarget JudgeMilestoneResultEntity entity);
}
