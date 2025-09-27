package org.bn.sensation.core.participant.service.mapper;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.RoundParticipantsDto;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface RoundParticipantsDtoMapper {

    @Mapping(target = "round", source = "round")
    @Mapping(target = "participants", source = "participants")
    RoundParticipantsDto toDto(RoundEntity round, List<ParticipantEntity> participants);

}
