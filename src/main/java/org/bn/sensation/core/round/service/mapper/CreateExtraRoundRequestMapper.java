package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.round.entity.ExtraRoundEntity;
import org.bn.sensation.core.round.service.dto.CreateExtraRoundRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateExtraRoundRequestMapper extends BaseDtoMapper<ExtraRoundEntity, CreateExtraRoundRequest> {
    @Override
    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "participants", ignore = true)
    ExtraRoundEntity toEntity(CreateExtraRoundRequest dto);

    @Override
    @Mapping(target = "milestoneId", source = "milestone.id")
    @Mapping(target = "participantId", source = "participant.id")
    CreateExtraRoundRequest toDto(ExtraRoundEntity entity);

}
