package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.assistant.service.mapper.AssistantDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, AssistantDtoMapper.class})
public interface ParticipantDtoMapper extends BaseDtoMapper<ParticipantEntity, ParticipantDto> {

    @Override
    ParticipantEntity toEntity(ParticipantDto dto);

    @Override
    ParticipantDto toDto(ParticipantEntity entity);
}
