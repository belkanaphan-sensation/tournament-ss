package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateRoundRequestMapper extends BaseDtoMapper<RoundEntity, CreateRoundRequest> {
    @Override
    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "participants", ignore = true)
    RoundEntity toEntity(CreateRoundRequest dto);


}
