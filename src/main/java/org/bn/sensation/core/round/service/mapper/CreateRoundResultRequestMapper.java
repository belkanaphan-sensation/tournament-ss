package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateRoundResultRequestMapper extends BaseDtoMapper<RoundResultEntity, CreateRoundResultRequest> {

    @Override
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriteria", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    RoundResultEntity toEntity(CreateRoundResultRequest dto);

    @Override
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "roundId", source = "round.id")
    @Mapping(target = "milestoneCriteriaId", source = "milestoneCriteria.id")
    CreateRoundResultRequest toDto(RoundResultEntity entity);

}
