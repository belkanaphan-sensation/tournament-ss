package org.bn.sensation.core.judgemilstoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.judgemilstoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface JudgeMilestoneResultRoundRequestMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultRoundRequest> {

    @Override
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriteria", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    JudgeMilestoneResultEntity toEntity(JudgeMilestoneResultRoundRequest dto);

    @Override
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "roundId", source = "round.id")
    @Mapping(target = "milestoneCriteriaId", source = "milestoneCriteria.id")
    JudgeMilestoneResultRoundRequest toDto(JudgeMilestoneResultEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriteria", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    void updateJudgeMilestoneResultRoundFromRequest(JudgeMilestoneResultRoundRequest request, @MappingTarget JudgeMilestoneResultEntity entity);
}
