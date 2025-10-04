package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.participant.service.dto.ParticipantRoundResultDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface ParticipantRoundResultDtoMapper extends BaseDtoMapper<ParticipantRoundResultEntity, ParticipantRoundResultDto> {

    @Override
    ParticipantRoundResultEntity toEntity(ParticipantRoundResultDto dto);

    @Override
    ParticipantRoundResultDto toDto(ParticipantRoundResultEntity entity);
}
