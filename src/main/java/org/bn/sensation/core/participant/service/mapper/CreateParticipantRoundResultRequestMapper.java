package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRoundResultRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateParticipantRoundResultRequestMapper extends BaseDtoMapper<ParticipantRoundResultEntity, CreateParticipantRoundResultRequest> {

    @Override
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "milestoneCriteria", ignore = true)
    @Mapping(target = "activityUser", ignore = true)
    ParticipantRoundResultEntity toEntity(CreateParticipantRoundResultRequest dto);

    @Override
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "roundId", source = "round.id")
    @Mapping(target = "milestoneCriteriaId", source = "milestoneCriteria.id")
    CreateParticipantRoundResultRequest toDto(ParticipantRoundResultEntity entity);

}
