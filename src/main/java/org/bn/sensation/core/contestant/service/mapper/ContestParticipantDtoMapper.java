package org.bn.sensation.core.contestant.service.mapper;

import org.bn.sensation.core.assistant.service.mapper.AssistantDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.contestant.service.dto.ContestParticipantDto;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {AssistantDtoMapper.class})
public interface ContestParticipantDtoMapper extends BaseDtoMapper<ParticipantEntity, ContestParticipantDto> {

    @Override
    @Mapping(source = "id", target = "participantId")
    @Mapping(source = "number", target = "number")
    @Mapping(source = "partnerSide", target = "partnerSide")
    @Mapping(source = "person.name", target = "name")
    @Mapping(source = "person.surname", target = "surname")
    ContestParticipantDto toDto(ParticipantEntity entity);
}
